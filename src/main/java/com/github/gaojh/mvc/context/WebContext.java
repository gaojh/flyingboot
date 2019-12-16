package com.github.gaojh.mvc.context;

import cn.hutool.core.util.StrUtil;
import com.github.gaojh.mvc.setup.SetupContext;
import com.github.gaojh.mvc.setup.SetupRunner;
import com.github.gaojh.mvc.annotation.*;
import com.github.gaojh.mvc.interceptor.HandlerInterceptor;
import com.github.gaojh.mvc.interceptor.InterceptorContext;
import com.github.gaojh.mvc.route.RouteContext;
import com.github.gaojh.mvc.route.RouteDefine;
import com.github.gaojh.mvc.utils.ClassUtils;
import com.github.gaojh.ioc.context.*;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 高建华
 * @date 2019-04-29 13:56
 */
@Slf4j
public class WebContext {

    private AppContext appContext;

    public WebContext(AppContext appContext) {
        this.appContext = appContext;
        initWebContext();
    }

    private void initWebContext() {
        initRoutes();
        initInterceptor();
        initApplicationRunners();
    }

    private void initRoutes() {
        List<Class<?>> controllers = appContext.getClassDefineByAnnotation(Controller.class);
        for (Class<?> clazz : controllers) {
            Object object = appContext.getBean(clazz);

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
                        RouteDefine routeDefine = new RouteDefine();
                        routeDefine.setType(clazz);
                        routeDefine.setObject(object);
                        routeDefine.setMethod(method);
                        routeDefine.setRequestMethod(methodRequestMapping.method());
                        routeDefine.setPath(url);
                        routeDefine.setParamNames(ClassUtils.getMethodParamNames(method));
                        routeDefine.setParams(new Object[method.getParameterCount()]);
                        RouteContext.addRoute(url, routeDefine);
                    }
                }

            }
        }
    }


    private void initInterceptor() {
        List<Class<?>> interceptors = appContext.getClassDefineByAnnotation(Interceptor.class);

        for (Class<?> clazz : interceptors) {
            Interceptor interceptor = clazz.getAnnotation(Interceptor.class);
            HandlerInterceptor obj = (HandlerInterceptor) appContext.getBean(clazz);
            for (String path : interceptor.pathPatterns()) {
                if (StrUtil.isNotBlank(path)) {
                    log.debug("注册拦截器：{} ===> {}", path, clazz.getName());
                    InterceptorContext.addInterceptor(interceptor, obj);
                }
            }
        }
    }

    private void initApplicationRunners() {
        List<Class<?>> setups = appContext.getClassDefineByAnnotation(Setup.class);
        for (Class<?> clazz : setups) {
            Setup setup = clazz.getAnnotation(Setup.class);
            SetupRunner obj = (SetupRunner) appContext.getBean(clazz);
            log.debug("注册启动后处理器：{}", clazz.getName());
            SetupContext.addSetupRunner(setup.order(), obj);
        }
        startApplicationRunners();
    }

    private void startApplicationRunners() {
        SetupContext.getSetupRunners().forEach(SetupRunner::run);
    }

}
