package com.github.gaojh.mvc.context;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.github.gaojh.context.ApplicationContext;
import com.github.gaojh.mvc.ApplicationRunner;
import com.github.gaojh.mvc.annotation.*;
import com.github.gaojh.mvc.interceptor.HandlerInterceptor;
import com.github.gaojh.mvc.route.DynamicRoute;
import com.github.gaojh.mvc.route.WebFactory;
import com.github.gaojh.mvc.route.WebRoute;
import com.github.gaojh.mvc.utils.ClassUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 高建华
 * @date 2019-04-29 13:56
 */
public class WebContext extends WebFactory {

    private static final Logger logger = LoggerFactory.getLogger(WebContext.class);

    private ApplicationContext applicationContext;

    public WebContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void initWebContext() {
        initRoutes();
        initInterceptor();
        initApplicationRunners();
    }

    public void addDynamicRoute(DynamicRoute dynamicRoute) {
        WebRoute webRoute = new WebRoute();
        webRoute.setRequestMethod(new RequestMethod[]{dynamicRoute.getRequestMethod()});
        webRoute.setType(dynamicRoute.getHandlerClass());
        webRoute.setUrlMapping(dynamicRoute.getPath());
        webRoute.setObject(applicationContext.getBean(dynamicRoute.getHandlerClass()));
        webRoute.setParams(new Object[1]);
        webRoute.setMethod(ClassUtil.getDeclaredMethod(dynamicRoute.getHandlerClass(), "handle", FullHttpRequest.class));
        putRoute(dynamicRoute.getPath(), webRoute);
        logger.debug("注册动态路由：{} ===> {}", dynamicRoute.getPath(), dynamicRoute.getHandlerClass().getName());
    }

    private void initRoutes() {
        List<Class<?>> controllers = applicationContext.getBeanClassOfAnnotation(Controller.class);
        for (Class<?> clazz : controllers) {
            Object object = applicationContext.getBean(clazz);

            RequestMapping controllerRequestMapping = clazz.getAnnotation(RequestMapping.class);
            String[] ctrlPath = controllerRequestMapping == null ? new String[]{"/"} : controllerRequestMapping.value();

            Method[] methods = clazz.getDeclaredMethods();

            List<Method> methodList = Arrays.stream(methods).filter(method -> method.isAnnotationPresent(RequestMapping.class)).collect(Collectors.toList());

            for (Method method : methodList) {
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                String[] methodPath = methodRequestMapping.value().length == 0 ? new String[]{""} : methodRequestMapping.value();
                for (String path : ctrlPath) {
                    for (String mpath : methodPath) {
                        String url = path + mpath;
                        if (!StrUtil.startWith(url, "/")) {
                            url = "/" + url;
                        }
                        url = StrUtil.replace(url, "//", "/");
                        WebRoute webRoute = new WebRoute(clazz, object, method, url, methodRequestMapping.method());
                        webRoute.setParamNames(ClassUtils.getMethodParamNames(method));
                        webRoute.setParams(new Object[method.getParameterCount()]);
                        putRoute(url, webRoute);
                        logger.debug("注册路由器：{} ===> {}", url, clazz.getName() + "." + method.getName());
                    }
                }

            }
        }
    }


    private void initInterceptor() {
        List<Class<?>> interceptors = applicationContext.getBeanClassOfAnnotation(Interceptor.class);

        for (Class<?> clazz : interceptors) {
            Interceptor interceptor = clazz.getAnnotation(Interceptor.class);
            HandlerInterceptor obj = (HandlerInterceptor) applicationContext.getBean(clazz);
            for (String path : interceptor.pathPatterns()) {
                if (StrUtil.isNotBlank(path)) {
                    logger.debug("注册拦截器：{} ===> {}", path, clazz.getName());
                    putInterceptor(path, obj);
                }
            }
        }
    }

    private void initApplicationRunners() {
        List<Class<?>> setups = applicationContext.getBeanClassOfAnnotation(Setup.class);
        for (Class<?> clazz : setups) {
            Setup setup = clazz.getAnnotation(Setup.class);
            ApplicationRunner obj = (ApplicationRunner) applicationContext.getBean(clazz);
            putApplicationRunner(setup.order(), obj);
        }
        startApplicationRunners();
    }

    private void startApplicationRunners() {
        getApplicationRunners().parallelStream().forEach(ApplicationRunner::run);
    }

}
