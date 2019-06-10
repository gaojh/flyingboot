package com.github.gaojh.mvc.context;

import cn.hutool.core.util.StrUtil;
import com.github.gaojh.mvc.ApplicationRunner;
import com.github.gaojh.mvc.annotation.*;
import com.github.gaojh.mvc.interceptor.HandlerInterceptor;
import com.github.gaojh.mvc.route.Route;
import com.github.gaojh.mvc.route.RouterFunction;
import com.github.gaojh.mvc.route.WebFactory;
import com.github.gaojh.mvc.utils.ClassUtils;
import com.github.gaojh.ioc.context.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        initWebContext();
    }

    public void removeRoute(String url){
        super.remoteRoute(url);
    }

    private void initWebContext() {
        initRoutes();
        initInterceptor();
        initApplicationRunners();
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
                        Route route = Route.builder().type(clazz).object(object).method(method).requestMethod(methodRequestMapping.method()).paramNames(ClassUtils.getMethodParamNames(method)).params(new Object[method.getParameterCount()]).build();
                        putRoute(url, route);
                    }
                }

            }
        }

        RouterFunction routerFunction = applicationContext.getBean(RouterFunction.class);
        Optional.ofNullable(routerFunction).ifPresent(rf -> rf.getRouteList().forEach(webRoute -> putRoute(webRoute.getUrlMapping(), webRoute)));
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
