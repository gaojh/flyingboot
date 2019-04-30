//package com.github.gaojh.context;
//
//import com.github.gaojh.ioc.annotation.Autowired;
//import cn.hutool.core.convert.Convert;
//import cn.hutool.core.util.ClassUtil;
//import cn.hutool.core.util.ReflectUtil;
//import cn.hutool.core.util.StrUtil;
//import com.github.gaojh.config.ApplicationConfig;
//import com.github.gaojh.config.Environment;
//import com.github.gaojh.ioc.annotation.Component;
//import com.github.gaojh.ioc.annotation.Configuration;
//import com.github.gaojh.ioc.annotation.Value;
//import com.github.gaojh.ioc.bean.BeanDefine;
//import com.github.gaojh.mvc.ApplicationRunner;
//import com.github.gaojh.mvc.annotation.*;
//import com.github.gaojh.mvc.route.WebRoute;
//import com.github.gaojh.mvc.interceptor.HandlerInterceptor;
//import com.github.gaojh.mvc.utils.ClassUtils;
//import com.github.gaojh.mvc.utils.PathMatcher;
//import io.netty.util.concurrent.DefaultThreadFactory;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.stream.Collectors;
//
///**
// * @author 高建华
// * @date 2019-03-31 14:04
// * 容器上下文
// */
//public class ApplicationContextBak {
//
//    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextBak.class);
//
//    private static ConcurrentHashMap<String, WebRoute> routeMap = new ConcurrentHashMap<>();
//    private static ConcurrentHashMap<String, Class<?>> beanClassMap = new ConcurrentHashMap<>();
//
//    private static ConcurrentHashMap<String, BeanDefine> beanDefineMap = new ConcurrentHashMap<>();
//
//    /**
//     * 初始化过程中临时存放
//     */
//    private ConcurrentHashMap<String, BeanDefine> initBeanDefineMap = new ConcurrentHashMap<>();
//
//    private TreeMap<Integer, ApplicationRunner> setupMap = new TreeMap<>();
//    private ConcurrentHashMap<String, List<HandlerInterceptor>> interceptorMap = new ConcurrentHashMap<>();
//
//    private ExecutorService executorService;
//
//    private Environment environment;
//
//    public ApplicationContextBak(Environment environment) {
//        this.environment = environment;
//        beanDefineMap.put(Environment.class.getName(), new BeanDefine(environment));
//
//        executorService = new ThreadPoolExecutor(ApplicationConfig.THREAD_POOL_CORE_SIZE,
//                ApplicationConfig.THREAD_POOL_MAX_SIZE,
//                ApplicationConfig.THREAD_POOL_KEEP_ALIVE_TIME,
//                TimeUnit.SECONDS,
//                new SynchronousQueue<>(),
//                new DefaultThreadFactory("Flying-pool"));
//
//        try {
//            initBeans();
//            initController();
//            initInterceptor();
//            initSetup();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 获取通用连接池
//     *
//     * @return
//     */
//    public ExecutorService getExecutorService() {
//        return executorService;
//    }
//
//    /**
//     * 根据请求地址获取路由信息
//     *
//     * @param url 请求url
//     * @return
//     */
//    public WebRoute getHttpRoute(String url) {
//        WebRoute webRoute = routeMap.get(url);
//        if (webRoute == null) {
//            Optional<WebRoute> optional = routeMap.entrySet().stream().filter(entry -> PathMatcher.me.isPattern(entry.getKey()) && PathMatcher.me.match(entry.getKey(), url)).map(Map.Entry::getValue).findFirst();
//            if (optional.isPresent()) {
//                webRoute = optional.get();
//
//                //直接存起来，下次直接拿，提高效率
//                routeMap.put(url, webRoute);
//            }
//
//
//        }
//        return webRoute;
//    }
//
//    public List<ApplicationRunner> getApplicationRunners() {
//        Set<Integer> keys = setupMap.keySet();
//        List<ApplicationRunner> list = new ArrayList<>();
//        for (Integer idx : keys) {
//            list.add(setupMap.get(idx));
//        }
//        return list;
//    }
//
//    /**
//     * 根据请求路径获取过滤器列表
//     *
//     * @param path
//     * @return
//     */
//    public List<HandlerInterceptor> getInterceptor(String path) {
//        List<HandlerInterceptor> list = interceptorMap.get(path);
//        if (list == null) {
//            Optional<List<HandlerInterceptor>> optional = interceptorMap.entrySet().stream().filter(entry -> PathMatcher.me.isPattern(entry.getKey()) && PathMatcher.me.match(entry.getKey(), path)).map(Map.Entry::getValue).findFirst();
//            list = optional.orElse(Collections.emptyList());
//        }
//
//        return list;
//    }
//
//
//    private void initBeans() {
//        logger.debug("开始初始化Bean");
//        Set<Class<?>> beans = new HashSet<>();
//
//        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
//            beans.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Component.class));
//            beans.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Controller.class));
//        }
//        //先将有注解bean的class以及其接口，都放入map中待用
//        beans.forEach(clazz -> {
//            beanClassMap.put(clazz.getName(), clazz);
//            Class<?>[] interfaces = clazz.getInterfaces();
//            if (interfaces.length > 0) {
//                for (Class<?> interfaceClazz : interfaces) {
//                    beanClassMap.put(interfaceClazz.getName(), clazz);
//                }
//            }
//        });
//
//        //开始初始化bean
//        for (Class clazz : beans) {
//            if (clazz.isAnnotationPresent(Component.class)) {
//                Component component = (Component) clazz.getAnnotation(Component.class);
//                String name = StrUtil.isBlank(component.value()) ? clazz.getName() : component.value();
//                BeanDefine beanDefine = createBean(clazz);
//                beanDefineMap.put(name, beanDefine);
//            } else if (clazz.isAnnotationPresent(Controller.class)) {
//                Controller controller = (Controller) clazz.getAnnotation(Controller.class);
//                String name = StrUtil.isBlank(controller.value()) ? clazz.getName() : controller.value();
//                BeanDefine beanDefine = createBean(clazz);
//                beanDefineMap.put(name, beanDefine);
//            }
//        }
//
//        logger.debug("初始化Bean完成");
//    }
//
//    private BeanDefine createBean(Class<?> clazz) {
//        String name = clazz.getName();
//        if (clazz.isAnnotationPresent(Component.class)) {
//            Component component = clazz.getAnnotation(Component.class);
//            name = StrUtil.isBlank(component.value()) ? clazz.getName() : component.value();
//        } else if (clazz.isAnnotationPresent(Controller.class)) {
//            Controller controller = clazz.getAnnotation(Controller.class);
//            name = StrUtil.isBlank(controller.value()) ? clazz.getName() : controller.value();
//        }else if(clazz.isAnnotationPresent(Configuration.class)){
//            Configuration configuration = clazz.getAnnotation(Configuration.class);
//            name = StrUtil.isBlank(configuration.value()) ? clazz.getName() : configuration.value();
//        }
//
//        if (beanDefineMap.containsKey(name)) {
//            return beanDefineMap.get(name);
//        }
//
//        Class<?>[] interfaces = clazz.getInterfaces();
//        if (interfaces.length > 0) {
//            for (Class<?> interfaceClazz : interfaces) {
//                if (beanDefineMap.containsKey(interfaceClazz.getName())) {
//                    beanDefineMap.put(name, beanDefineMap.get(interfaceClazz.getName()));
//                    return beanDefineMap.get(name);
//                }
//            }
//        }
//
//        Object obj = null;
//
//        //构造函数注入
//        List<Constructor> constructorList = Arrays.stream(clazz.getConstructors()).filter(constructor -> constructor.isAnnotationPresent(Autowired.class)).collect(Collectors.toList());
//        if (constructorList.size() == 0) {
//            try {
//                obj = clazz.newInstance();
//            } catch (InstantiationException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        } else if (constructorList.size() == 1) {
//            logger.debug("使用构造函数实例化对象：{}", clazz.getName());
//            Constructor constructor = constructorList.get(0);
//            try {
//                obj = constructor.newInstance(getConstractorParameters(constructor));
//            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//
//        } else {
//            throw new RuntimeException("@Autowried只能注入一个构造方法");
//        }
//
//        BeanDefine beanDefine = new BeanDefine(obj);
//
//        initBeanDefineMap.put(name, beanDefine);
//        Field[] fields = clazz.getDeclaredFields();
//        setFields(clazz, obj, fields);
//        initBeanDefineMap.clear();
//        return beanDefine;
//
//    }
//
//    /**
//     * 初始化controller
//     */
//    private void initController() {
//        logger.debug("开始初始化Controller");
//        Set<Class<?>> controllers = new HashSet<>();
//        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
//            controllers.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Controller.class));
//        }
//
//        for (Class clazz : controllers) {
//            Controller controller = (Controller) clazz.getAnnotation(Controller.class);
//            String name = StrUtil.isBlank(controller.value()) ? clazz.getName() : controller.value();
//            logger.debug("初始化：{}", name);
//            RequestMapping c = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
//            String[] ctrlPath = c == null ? new String[]{"/"} : c.value();
//            Object obj = beanDefineMap.get(name).getObject();
//
//            Method[] methods = clazz.getDeclaredMethods();
//            for (Method method : methods) {
//                RequestMapping rm = method.getAnnotation(RequestMapping.class);
//                if (rm != null) {
//                    String[] methodPath = rm.value().length == 0 ? new String[]{""} : rm.value();
//                    for (String path : ctrlPath) {
//                        for (String mpath : methodPath) {
//                            String url = path + mpath;
//                            if (!StrUtil.startWith(url, "/")) {
//                                url = "/" + url;
//                            }
//                            url = StrUtil.replace(url, "//", "/");
//                            WebRoute webRoute = new WebRoute();
//                            webRoute.setType(clazz);
//                            webRoute.setObject(obj);
//                            webRoute.setMethod(method);
//                            webRoute.setUrlMapping(url);
//                            webRoute.setParamNames(ClassUtils.getMethodParamNames(method));
//                            webRoute.setParams(new Object[method.getParameterCount()]);
//                            webRoute.setRequestMethod(rm.method());
//                            if (rm.method().equals(RequestMethod.GET)) {
//                                routeMap.put(url, webRoute);
//                            } else if (rm.method().equals(RequestMethod.POST)) {
//                                routeMap.put(url, webRoute);
//                            } else {
//                                routeMap.put(url, webRoute);
//                                routeMap.put(url, webRoute);
//                            }
//                            logger.debug("注册：{} --> {}", url, clazz.getName() + "." + method.getName());
//                        }
//                    }
//
//                }
//            }
//        }
//        logger.debug("初始化Controller完成");
//    }
//
//    private void setFields(Class clazz, Object obj, Field[] fields) {
//        for (Field field : fields) {
//            Autowired autowired = field.getAnnotation(Autowired.class);
//            Value value = field.getAnnotation(Value.class);
//            String fieldName = field.getType().getName();
//            if (autowired != null) {
//                BeanDefine fieldObj = beanDefineMap.get(fieldName);
//                if (fieldObj == null) {
//                    fieldObj = initBeanDefineMap.get(fieldName);
//                }
//                if (fieldObj == null) {
//                    fieldObj = createBean(beanClassMap.get(fieldName));
//                    beanDefineMap.put(fieldName, fieldObj);
//                }
//                ReflectUtil.setFieldValue(obj, field, fieldObj.getObject());
//            } else if (value != null) {
//                setFieldValue(clazz, obj, field, value);
//            }
//        }
//    }
//
//    private void setFieldValue(Class clazz, Object obj, Field field, Value value) {
//        String valueStr = value.value();
//        if (StrUtil.isBlank(valueStr)) {
//            logger.error("{}中的Field：{}无法设值！", clazz.getName(), field.getName());
//        } else if (!StrUtil.containsAny(valueStr, "${", "}")) {
//            logger.error("{}中的Field：{}表达式格式不正确！", clazz.getName(), field.getName());
//        } else {
//            String key = StrUtil.subBetween(valueStr, "${", "}");
//            ReflectUtil.setFieldValue(obj, field, environment.getString(key));
//        }
//    }
//
//    private Object[] getConstractorParameters(Constructor constructor) {
//        Class[] parameters = constructor.getParameterTypes();
//        return Arrays.stream(parameters).map(cls -> {
//            String parameterName = cls.getName();
//            BeanDefine beanDefine = beanDefineMap.get(parameterName);
//            if (beanDefine == null) {
//                //初始化过程中的
//                beanDefine = initBeanDefineMap.get(parameterName);
//            }
//            if (beanDefine == null) {
//                beanDefine = createBean(beanClassMap.get(parameterName));
//                beanDefineMap.put(parameterName, beanDefine);
//            }
//            return Convert.convert(beanDefine.getType(), beanDefine.getObject());
//        }).toArray();
//    }
//
//
//    private void initInterceptor() throws Exception {
//        logger.debug("开始初始化Interceptor");
//        Set<Class<?>> interceptors = new HashSet<>();
//        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
//            interceptors.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Interceptor.class));
//        }
//        for (Class<?> clazz : interceptors) {
//            Interceptor interceptor = clazz.getAnnotation(Interceptor.class);
//            HandlerInterceptor obj = (HandlerInterceptor) clazz.newInstance();
//            for (String path : interceptor.pathPatterns()) {
//                if (StringUtils.isNotBlank(path)) {
//                    List<HandlerInterceptor> list = interceptorMap.getOrDefault(path, new ArrayList<>());
//                    list.add(obj);
//                    interceptorMap.put(path, list);
//                }
//            }
//
//            //设置field
//            Field[] fields = clazz.getDeclaredFields();
//            setFields(clazz, obj, fields);
//        }
//        logger.debug("初始化Interceptor完成");
//    }
//
//    private void initSetup() throws Exception {
//        Set<Class<?>> setups = new HashSet<>();
//        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
//            setups.addAll(ClassUtil.scanPackageByAnnotation(basePkg, Setup.class));
//        }
//        for (Class<?> clazz : setups) {
//            if (clazz.isAnnotationPresent(Component.class)) {
//                BeanDefine beanDefine = beanDefineMap.get(clazz.getName());
//                Setup setup = clazz.getAnnotation(Setup.class);
//                ApplicationRunner obj = (ApplicationRunner) beanDefine.getObject();
//                setupMap.put(setup.order(), obj);
//
//                //设置field
//                Field[] fields = clazz.getDeclaredFields();
//                setFields(clazz, obj, fields);
//            } else {
//                logger.debug("Setup: {}，需要添加注解@Commonent", clazz.getName());
//            }
//        }
//
//    }
//
//
//    public static ConcurrentHashMap<String, BeanDefine> getBeanDefineMap() {
//        return beanDefineMap;
//    }
//
//    public static void setBeanDefineMap(ConcurrentHashMap<String, BeanDefine> beanDefineMap) {
//        ApplicationContextBak.beanDefineMap = beanDefineMap;
//    }
//
//}
