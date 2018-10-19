package com.gao.flying.context;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import com.gao.flying.FlyingConst;
import com.gao.flying.ioc.annotation.Bean;
import com.gao.flying.ioc.annotation.Inject;
import com.gao.flying.ioc.annotation.Value;
import com.gao.flying.ioc.bean.BeanDefine;
import com.gao.flying.mvc.ApplicationRunner;
import com.gao.flying.mvc.annotation.Ctrl;
import com.gao.flying.mvc.annotation.Route;
import com.gao.flying.mvc.annotation.Setup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.*;

/**
 * @author 高建华
 * @date 2018/6/22 下午2:43
 */
@NoArgsConstructor
public class ServerContext {

    @Setter
    @Getter
    private Props props;

    @Getter
    private ConcurrentHashMap<String, com.gao.flying.mvc.http.Route> routeMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Class<?>> beanClassMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, BeanDefine> beanDefineMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefine> initBeanDefineMap = new ConcurrentHashMap<>();
    private TreeMap<Integer, ApplicationRunner> setupMap = new TreeMap<>();

    @Getter
    private ExecutorService executorService;


    private Reflections reflections;

    private static Log log = LogFactory.get();

    public ServerContext(Props props) {
        this.props = props;

        if (props.getStr(FlyingConst.BASE_PACKAGE_STRING) == null) {
            throw new RuntimeException("需要配置" + FlyingConst.BASE_PACKAGE_STRING);
        }

        executorService = new ThreadPoolExecutor(props.getInt(FlyingConst.DISPATHCER_THREAD_CORE_SIZE, 600), props.getInt(FlyingConst.DISPATHCER_THREAD_MAX_SIZE, 1500), 0, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), new DefaultThreadFactory("dispatcher-pool"));

        reflections = new Reflections(props.getStr(FlyingConst.BASE_PACKAGE_STRING));
        try {
            initBeans();
            initController();
            initSetup();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public com.gao.flying.mvc.http.Route getRoute(String url) {
        return routeMap.get(url);
    }

    /**
     * 初始化bean
     */
    private void initBeans() throws Exception {
        log.info("开始初始化Bean");
        Set<Class<?>> beans = reflections.getTypesAnnotatedWith(Bean.class);

        //先将有注解bean的class以及其接口，都放入map中待用
        beans.forEach(clazz -> {

            beanClassMap.put(clazz.getName(), clazz);
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                for (Class<?> interfaceClazz : interfaces) {
                    beanClassMap.put(interfaceClazz.getName(), clazz);
                }
            }
        });

        //开始初始化bean
        for (Class clazz : beans) {
            Bean bean = (Bean) clazz.getAnnotation(Bean.class);
            String name = StrUtil.isBlank(bean.name()) ? clazz.getName() : bean.name();
            BeanDefine beanDefine = createBean(clazz);
            beanDefineMap.put(name, beanDefine);
        }

        log.info("初始化Bean完成");
    }

    private BeanDefine createBean(Class clazz) throws Exception {
        Bean bean = (Bean) clazz.getAnnotation(Bean.class);
        String name = StrUtil.isBlank(bean.name()) ? clazz.getName() : bean.name();
        if (beanDefineMap.containsKey(name)) {
            log.debug("{}已经初始化，跳过！", name);
            return beanDefineMap.get(name);
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> interfaceClazz : interfaces) {
                if (beanDefineMap.containsKey(interfaceClazz.getName())) {
                    log.debug("{}已经初始化，跳过！", name);
                    beanDefineMap.put(name, beanDefineMap.get(interfaceClazz.getName()));
                    return beanDefineMap.get(name);
                }
            }
        }
        log.info("初始化：{}", name);
        Object obj = clazz.newInstance();
        BeanDefine beanDefine = new BeanDefine(obj);
        beanDefine.setSingle(bean.singleton());

        initBeanDefineMap.put(name, beanDefine);

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Inject inject = field.getAnnotation(Inject.class);
            Value value = field.getAnnotation(Value.class);
            String fieldName = field.getType().getName();
            if (inject != null) {
                log.debug("设置Field：{}", fieldName);
                field.setAccessible(true);
                BeanDefine fieldObj = initBeanDefineMap.get(fieldName);
                if (fieldObj == null) {
                    fieldObj = createBean(beanClassMap.get(fieldName));
                    beanDefineMap.put(fieldName, fieldObj);
                }
                field.set(obj, fieldObj.getBean());

            } else if (value != null) {
                setFieldValue(clazz, obj, field, value);
            }

        }
        initBeanDefineMap.clear();
        return beanDefine;

    }

    /**
     * 初始化Controller
     */
    private void initController() throws Exception {
        log.info("开始初始化Controller");
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Ctrl.class);
        for (Class clazz : controllers) {
            Route c = (Route) clazz.getAnnotation(Route.class);
            String path = c == null ? "/" : c.value();
            Object obj = clazz.newInstance();

            //设置field
            Field[] fields = clazz.getDeclaredFields();
            setFields(clazz, obj, fields);


            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Route rm = method.getAnnotation(Route.class);
                if (rm != null) {
                    String url = path + (StrUtil.isEmpty(rm.value()) ? "" : rm.value());
                    if (!StrUtil.startWith(url, "/")) {
                        url = "/" + url;
                    }
                    url = StrUtil.replace(url, "//", "/");
                    log.info("注册：{} --> {}", url, clazz.getName() + "." + method.getName());
                    routeMap.putIfAbsent(url, new com.gao.flying.mvc.http.Route(clazz, obj, method));
                }
            }
        }
        log.info("初始化Controller完成");
    }

    private void setFields(Class clazz, Object obj, Field[] fields) throws IllegalAccessException {
        for (Field field : fields) {
            Inject inject = field.getAnnotation(Inject.class);
            Value value = field.getAnnotation(Value.class);
            String fieldName = field.getType().getName();
            if (inject != null) {
                log.debug("设置Field：{}", fieldName);
                field.setAccessible(true);
                BeanDefine fieldBeanDefine = beanDefineMap.get(fieldName);
                if (fieldBeanDefine == null) {
                    throw new RuntimeException(fieldName + "还未初始化");
                }
                field.set(obj, fieldBeanDefine.getBean());
            } else if (value != null) {
                setFieldValue(clazz, obj, field, value);
            }
        }
    }

    private void setFieldValue(Class clazz, Object obj, Field field, Value value) throws IllegalAccessException {
        String valueStr = value.value();
        if (StrUtil.isBlank(valueStr)) {
            log.error("{}中的Field：{}无法设值！", clazz.getName(), field.getName());
        } else if (!StrUtil.containsAny(valueStr, "${", "}")) {
            log.error("{}中的Field：{}表达式格式不正确！", clazz.getName(), field.getName());
        } else {
            String key = StrUtil.subBetween(valueStr, "${", "}");
            field.setAccessible(true);
            switch (field.getType().getName()) {
                case "java.lang.String":
                    field.set(obj, props.getStr(key));
                    break;
                case "java.lang.Integer":
                    field.set(obj, props.getInt(key));
                    break;
                case "int":
                    field.set(obj, props.getInt(key));
                    break;
                case "java.lang.Double":
                    field.set(obj, props.getDouble(key));
                    break;
                case "double":
                    field.set(obj, props.getDouble(key));
                    break;
                case "java.lang.Float":
                    field.set(obj, props.getFloat(key));
                    break;
                case "float":
                    field.set(obj, props.getFloat(key));
                    break;
                case "java.lang.Boolean":
                    field.set(obj, props.getBool(key));
                    break;
                case "boolean":
                    field.set(obj, props.getBool(key));
                    break;
                case "java.lang.Long":
                    field.set(obj, props.getLong(key));
                    break;
                case "long":
                    field.set(obj, props.getLong(key));
                    break;
                case "java.lang.Short":
                    field.set(obj, props.getShort(key));
                    break;
                case "short":
                    field.set(obj, props.getShort(key));
                    break;
                case "java.math.BigDecimal":
                    field.set(obj, props.getBigDecimal(key));
                    break;
                case "java.math.BigInteger":
                    field.set(obj, props.getBigInteger(key));
                    break;
                case "java.lang.Char":
                    field.set(obj, props.getChar(key));
                    break;
                case "char":
                    field.set(obj, props.getChar(key));
                    break;
                default:
                    field.set(obj, props.get(key));
                    break;
            }

        }
    }

    private void initSetup() throws Exception {
        Set<Class<?>> setups = reflections.getTypesAnnotatedWith(Setup.class);
        for (Class<?> clazz : setups) {
            Setup setup = clazz.getAnnotation(Setup.class);
            ApplicationRunner obj = (ApplicationRunner) clazz.newInstance();
            setupMap.put(setup.order(), obj);

            //设置field
            Field[] fields = clazz.getDeclaredFields();
            setFields(clazz, obj, fields);
        }

        executeSetup();
    }

    private void executeSetup() {
        //执行
        for (Map.Entry<Integer, ApplicationRunner> entry : setupMap.entrySet()) {
            entry.getValue().run();
        }
    }

}
