package com.gao.flying.context;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gao.flying.config.ApplicationConfig;
import com.gao.flying.config.ApplicationEnvironment;
import com.gao.flying.ioc.annotation.Autowired;
import com.gao.flying.ioc.annotation.Component;
import com.gao.flying.ioc.annotation.Value;
import com.gao.flying.ioc.bean.BeanDefine;
import com.gao.flying.mvc.ApplicationRunner;
import com.gao.flying.mvc.annotation.Controller;
import com.gao.flying.mvc.annotation.Interceptor;
import com.gao.flying.mvc.annotation.RequestMapping;
import com.gao.flying.mvc.annotation.Setup;
import com.gao.flying.mvc.http.HttpRoute;
import com.gao.flying.mvc.interceptor.HandlerInterceptor;
import com.gao.flying.mvc.utils.ClassUtils;
import com.gao.flying.mvc.utils.PathMatcher;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 高建华
 * @date 2019-03-31 14:04
 * 容器上下文
 */
public class ApplicationContext {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    private static ConcurrentHashMap<String, HttpRoute> routeMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Class<?>> beanClassMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, BeanDefine> beanDefineMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, BeanDefine> initBeanDefineMap = new ConcurrentHashMap<>();

    private TreeMap<Integer, ApplicationRunner> setupMap = new TreeMap<>();
    private ConcurrentHashMap<String, HandlerInterceptor> interceptorMap = new ConcurrentHashMap<>();

    @Getter
    private ApplicationEnvironment environment;

    public ApplicationContext(ApplicationEnvironment environment) {
        this.environment = environment;

        try {
            initBeans();
            initController();
            initInterceptor();
            initSetup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public HttpRoute getHttpRoute(String url) {
        HttpRoute httpRoute = routeMap.get(url);
        if (httpRoute == null) {
            Optional<HttpRoute> optional = routeMap.entrySet().stream().filter(entry -> PathMatcher.me.isPattern(entry.getKey()) && PathMatcher.me.match(entry.getKey(), url)).map(Map.Entry::getValue).findFirst();
            if (optional.isPresent()) {
                httpRoute = optional.get();
            }
        }
        return httpRoute;
    }

    public List<ApplicationRunner> getApplicationRunners() {
        Set<Integer> keys = setupMap.keySet();
        List<ApplicationRunner> list = new ArrayList<>();
        for (Integer idx : keys) {
            list.add(setupMap.get(idx));
        }
        return list;
    }

    public List<HandlerInterceptor> getInterceptor(String path) {
        return interceptorMap.entrySet().stream().filter(entry -> PathMatcher.me.match(entry.getKey(), path)).map(Map.Entry::getValue).collect(Collectors.toList());
    }


    private void initBeans() {
        logger.debug("开始初始化Bean");
        Set<Class<?>> beans = new HashSet<>();

        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
            beans.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Component.class));
            beans.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Controller.class));
        }
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
            if (clazz.isAnnotationPresent(Component.class)) {
                Component component = (Component) clazz.getAnnotation(Component.class);
                String name = StrUtil.isBlank(component.value()) ? clazz.getName() : component.value();
                BeanDefine beanDefine = createBean(clazz);
                beanDefineMap.put(name, beanDefine);
            } else if (clazz.isAnnotationPresent(Controller.class)) {
                Controller controller = (Controller) clazz.getAnnotation(Controller.class);
                String name = StrUtil.isBlank(controller.value()) ? clazz.getName() : controller.value();
                BeanDefine beanDefine = createBean(clazz);
                beanDefineMap.put(name, beanDefine);
            }
        }

        logger.debug("初始化Bean完成");
    }

    private BeanDefine createBean(Class clazz) {
        String name = clazz.getName();
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = (Component) clazz.getAnnotation(Component.class);
            name = StrUtil.isBlank(component.value()) ? clazz.getName() : component.value();
        } else if (clazz.isAnnotationPresent(Controller.class)) {
            Controller controller = (Controller) clazz.getAnnotation(Controller.class);
            name = StrUtil.isBlank(controller.value()) ? clazz.getName() : controller.value();
        }

        if (beanDefineMap.containsKey(name)) {
            logger.debug("{}已经初始化，跳过！", name);
            return beanDefineMap.get(name);
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> interfaceClazz : interfaces) {
                if (beanDefineMap.containsKey(interfaceClazz.getName())) {
                    logger.debug("{}已经初始化，跳过！", name);
                    beanDefineMap.put(name, beanDefineMap.get(interfaceClazz.getName()));
                    return beanDefineMap.get(name);
                }
            }
        }
        logger.debug("初始化：{}", name);
        Object obj = null;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        BeanDefine beanDefine = new BeanDefine(obj);

        initBeanDefineMap.put(name, beanDefine);
        Field[] fields = clazz.getDeclaredFields();
        setFields(clazz, obj, fields);
        initBeanDefineMap.clear();
        return beanDefine;

    }

    /**
     * 初始化controller
     */
    private void initController() {
        logger.debug("开始初始化Controller");
        Set<Class<?>> controllers = new HashSet<>();
        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
            controllers.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Controller.class));
        }

        for (Class clazz : controllers) {
            Controller controller = (Controller) clazz.getAnnotation(Controller.class);
            String name = StrUtil.isBlank(controller.value()) ? clazz.getName() : controller.value();

            RequestMapping c = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            String[] ctrlPath = c == null ? new String[]{"/"} : c.value();
            Object obj = beanDefineMap.get(name).getBean();

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                RequestMapping rm = method.getAnnotation(RequestMapping.class);
                if (rm != null) {
                    String[] methodPath = rm.value().length == 0 ? new String[]{""} : rm.value();
                    for (String path : ctrlPath) {
                        for (String mpath : methodPath) {
                            String url = path + mpath;
                            if (!StrUtil.startWith(url, "/")) {
                                url = "/" + url;
                            }
                            url = StrUtil.replace(url, "//", "/");
                            HttpRoute httpRoute = new HttpRoute();
                            httpRoute.setType(clazz);
                            httpRoute.setObject(obj);
                            httpRoute.setMethod(method);
                            httpRoute.setUrlMapping(url);
                            httpRoute.setParamNames(ClassUtils.getMethodParamNames(method));
                            httpRoute.setParams(new Object[method.getParameterCount()]);
                            httpRoute.setHttpMethod(rm.method());
                            if (rm.method().equals(RequestMapping.METHOD.GET)) {
                                routeMap.put(url, httpRoute);
                            } else if (rm.method().equals(RequestMapping.METHOD.POST)) {
                                routeMap.put(url, httpRoute);
                            } else {
                                routeMap.put(url, httpRoute);
                                routeMap.put(url, httpRoute);
                            }
                            logger.debug("注册：{} --> {}", url, clazz.getName() + "." + method.getName());
                        }
                    }

                }
            }
        }
        logger.debug("初始化Controller完成");
    }

    private void setFields(Class clazz, Object obj, Field[] fields) {
        for (Field field : fields) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            Value value = field.getAnnotation(Value.class);
            String fieldName = field.getType().getName();
            if (autowired != null) {
                logger.debug("设置Field：{}", fieldName);
                BeanDefine fieldObj = initBeanDefineMap.get(fieldName);
                if (fieldObj == null) {
                    fieldObj = createBean(beanClassMap.get(fieldName));
                    beanDefineMap.put(fieldName, fieldObj);
                }
                ReflectUtil.setFieldValue(obj, field, fieldObj.getBean());
            } else if (value != null) {
                setFieldValue(clazz, obj, field, value);
            }
        }
    }

    private void setFieldValue(Class clazz, Object obj, Field field, Value value) {
        String valueStr = value.value();
        if (StrUtil.isBlank(valueStr)) {
            logger.error("{}中的Field：{}无法设值！", clazz.getName(), field.getName());
        } else if (!StrUtil.containsAny(valueStr, "${", "}")) {
            logger.error("{}中的Field：{}表达式格式不正确！", clazz.getName(), field.getName());
        } else {
            String key = StrUtil.subBetween(valueStr, "${", "}");
            ReflectUtil.setFieldValue(obj, field, environment.getString(key));
        }
    }

    private void initInterceptor() throws Exception {
        logger.debug("开始初始化Interceptor");
        Set<Class<?>> interceptors = new HashSet<>();
        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
            interceptors.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Interceptor.class));
        }
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
        logger.debug("初始化Interceptor完成");
    }

    private void initSetup() throws Exception {
        Set<Class<?>> setups = new HashSet<>();
        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
            setups.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Setup.class));
        }
        for (Class<?> clazz : setups) {
            Setup setup = clazz.getAnnotation(Setup.class);
            ApplicationRunner obj = (ApplicationRunner) clazz.newInstance();
            setupMap.put(setup.order(), obj);

            //设置field
            Field[] fields = clazz.getDeclaredFields();
            setFields(clazz, obj, fields);
        }

    }

}
