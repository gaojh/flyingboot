package com.gao.flying.context;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import com.gao.flying.FlyingConst;
import com.gao.flying.ioc.annotation.Component;
import com.gao.flying.ioc.annotation.Autowired;
import com.gao.flying.ioc.annotation.Value;
import com.gao.flying.ioc.bean.BeanDefine;
import com.gao.flying.mvc.ApplicationRunner;
import com.gao.flying.mvc.annotation.*;
import com.gao.flying.mvc.http.HttpRoute;
import com.gao.flying.mvc.interceptor.HandlerInterceptor;
import com.gao.flying.mvc.utils.PathMatcher;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author 高建华
 * @date 2018/6/22 下午2:43
 */
@NoArgsConstructor
public class FlyingContext {

    @Setter
    @Getter
    private Props props;

    @Getter
    private ConcurrentHashMap<String, HttpRoute> routeGetMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, HttpRoute> routeGetPatternMap = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<String, HttpRoute> routePostMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, HttpRoute> routePostPatternMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Class<?>> beanClassMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, BeanDefine> beanDefineMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefine> initBeanDefineMap = new ConcurrentHashMap<>();
    private TreeMap<Integer, ApplicationRunner> setupMap = new TreeMap<>();
    private TreeMap<String, HandlerInterceptor> interceptorMap = new TreeMap<>();

    private ExecutorService executorService;


    private static Log log = LogFactory.get();

    public FlyingContext(Props props) {
        this.props = props;

        if (props.getStr(FlyingConst.BASE_PACKAGE_STRING) == null) {
            throw new RuntimeException("需要配置" + FlyingConst.BASE_PACKAGE_STRING);
        }

        executorService = ThreadUtil.newExecutor(600, 1200);
        //new ThreadPoolExecutor(props.getInt(FlyingConst.DISPATHCER_THREAD_CORE_SIZE, 600), props.getInt(FlyingConst.DISPATHCER_THREAD_MAX_SIZE, 1500), 0, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), new DefaultThreadFactory("dispatcher-pool"));

        try {
            initBeans();
            initInterceptor();
            initController();
            initSetup();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public HttpRoute fetchGetRoute(String url) {
        return getRoute(url, routeGetMap, routeGetPatternMap);
    }

    public HttpRoute fetchPostRoute(String url) {
        return getRoute(url, routePostMap, routePostPatternMap);
    }

    private HttpRoute getRoute(String url, ConcurrentHashMap<String, HttpRoute> routeMap, ConcurrentHashMap<String, HttpRoute> routePatternMap) {
        HttpRoute route = routeMap.get(url);
        if (route == null) {
            Optional<HttpRoute> optional = routePatternMap.entrySet().stream().filter(entry -> PathMatcher.me.match(entry.getKey(), url)).map(Map.Entry::getValue).findFirst();
            if (optional.isPresent()) {
                route = optional.get();
            }
        }
        return route;
    }

    /**
     * 初始化bean
     */
    private void initBeans() throws Exception {
        log.info("开始初始化Bean");
        Set<Class<?>> beans = ClassUtil.scanPackageByAnnotation(props.getStr(FlyingConst.BASE_PACKAGE_STRING), Component.class);
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
            Component component = (Component) clazz.getAnnotation(Component.class);
            String name = StrUtil.isBlank(component.value()) ? clazz.getName() : component.value();
            BeanDefine beanDefine = createBean(clazz);
            beanDefineMap.put(name, beanDefine);
        }

        log.info("初始化Bean完成");
    }

    private BeanDefine createBean(Class clazz) throws Exception {
        Component component = (Component) clazz.getAnnotation(Component.class);
        String name = StrUtil.isBlank(component.value()) ? clazz.getName() : component.value();
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

        initBeanDefineMap.put(name, beanDefine);

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            Value value = field.getAnnotation(Value.class);
            String fieldName = field.getType().getName();
            if (autowired != null) {
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

    private void initInterceptor() throws Exception {
        log.info("开始初始化Interceptor");
        Set<Class<?>> interceptors = ClassUtil.scanPackageByAnnotation(props.getStr(FlyingConst.BASE_PACKAGE_STRING), Interceptor.class);
        for (Class<?> clazz : interceptors) {
            Interceptor interceptor = clazz.getAnnotation(Interceptor.class);
            HandlerInterceptor obj = (HandlerInterceptor) clazz.newInstance();
            for (String path : interceptor.pathPatterns()) {
                if (StringUtils.isNotBlank(path)) {
                    interceptorMap.put(path, obj);
                }
            }

            //设置field
            Field[] fields = clazz.getDeclaredFields();
            setFields(clazz, obj, fields);
        }
        log.info("初始化Interceptor完成");
    }

    /**
     * 初始化controller
     * @throws Exception
     */
    private void initController() throws Exception {
        log.info("开始初始化Controller");
        Set<Class<?>> controllers = ClassUtil.scanPackageByAnnotation(props.getStr(FlyingConst.BASE_PACKAGE_STRING), Controller.class);
        for (Class clazz : controllers) {
            RequestMapping c = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            String[] ctrlPath = c == null ? new String[]{""} : c.value();
            Object obj = clazz.newInstance();
            //设置field
            Field[] fields = clazz.getDeclaredFields();
            setFields(clazz, obj, fields);

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                RequestMapping rm = method.getAnnotation(RequestMapping.class);
                if (rm != null) {
                    String[] methodPath = rm.value();
                    for(String path : ctrlPath){
                        for(String mpath : methodPath){
                            String url = path + mpath;
                            if (!StrUtil.startWith(url, "/")) {
                                url = "/" + url;
                            }
                            url = StrUtil.replace(url, "//", "/");

                            if (rm.method().equals(RequestMapping.METHOD.GET)) {
                                if (PathMatcher.me.isPattern(url)) {
                                    routeGetPatternMap.putIfAbsent(url, new HttpRoute(clazz, obj, method, url));
                                } else {
                                    routeGetMap.putIfAbsent(url, new HttpRoute(clazz, obj, method, url));
                                }
                            } else if (rm.method().equals(RequestMapping.METHOD.POST)) {
                                if (PathMatcher.me.isPattern(url)) {
                                    routePostPatternMap.putIfAbsent(url, new HttpRoute(clazz, obj, method, url));
                                } else {
                                    routePostMap.putIfAbsent(url, new HttpRoute(clazz, obj, method, url));
                                }
                            } else {
                                if (PathMatcher.me.isPattern(url)) {
                                    routeGetPatternMap.putIfAbsent(url, new HttpRoute(clazz, obj, method, url));
                                    routePostPatternMap.putIfAbsent(url, new HttpRoute(clazz, obj, method, url));
                                } else {
                                    routeGetMap.putIfAbsent(url, new HttpRoute(clazz, obj, method, url));
                                    routePostMap.putIfAbsent(url, new HttpRoute(clazz, obj, method, url));
                                }
                            }

                            log.info("注册：{} --> {}", url, clazz.getName() + "." + method.getName());
                        }
                    }

                }
            }
        }
        log.info("初始化Controller完成");
    }


    private void setFields(Class clazz, Object obj, Field[] fields) throws IllegalAccessException {
        for (Field field : fields) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            Value value = field.getAnnotation(Value.class);
            String fieldName = field.getType().getName();
            if (autowired != null) {
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
        Set<Class<?>> setups = ClassUtil.scanPackageByAnnotation(props.getStr(FlyingConst.BASE_PACKAGE_STRING), Setup.class);
        for (Class<?> clazz : setups) {
            Setup setup = clazz.getAnnotation(Setup.class);
            ApplicationRunner obj = (ApplicationRunner) clazz.newInstance();
            setupMap.put(setup.order(), obj);

            //设置field
            Field[] fields = clazz.getDeclaredFields();
            setFields(clazz, obj, fields);
        }

        //异步执行setup
        ThreadUtil.execute(this::executeSetup);
    }

    public void executeSetup() {
        //执行
        for (Map.Entry<Integer, ApplicationRunner> entry : setupMap.entrySet()) {
            entry.getValue().run();
        }
    }


    public List<HandlerInterceptor> getInterceptors(String path) {
        return interceptorMap.entrySet().stream().filter(entry -> PathMatcher.me.match(entry.getKey(), path)).map(Map.Entry::getValue).collect(Collectors.toList());
    }

}
