package com.github.gaojh.mvc.route;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.github.gaojh.config.Environment;
import com.github.gaojh.ioc.context.AppContext;
import com.github.gaojh.ioc.context.AppUtil;
import com.github.gaojh.mvc.annotation.PathParam;
import com.github.gaojh.mvc.annotation.RequestBody;
import com.github.gaojh.mvc.annotation.RequestMethod;
import com.github.gaojh.mvc.annotation.RequestParam;
import com.github.gaojh.mvc.utils.JsonTools;
import com.github.gaojh.mvc.utils.PathMatcher;
import com.github.gaojh.server.context.HttpContext;
import com.github.gaojh.server.http.HttpRequest;
import com.github.gaojh.server.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author 高建华
 * @date 2019-03-31 22:49
 */
@Slf4j
public class SimpleRouteHandler implements RouteHandler {

    private ExecutorService executorService;

    @Override
    public CompletableFuture<HttpContext> invoke(HttpContext httpContext, RouteDefine routeDefine) throws Exception {
        AppContext appContext = AppUtil.appContext;
        Environment environment = appContext.getEnvironment();
        this.executorService = environment.getExecutorService();
        setParams(httpContext, routeDefine);
        return invokeMethod(httpContext, routeDefine);
    }


    private void setParams(HttpContext httpContext, RouteDefine routeDefine) {
        HttpRequest httpRequest = httpContext.getHttpRequest();
        HttpResponse httpResponse = httpContext.getHttpResponse();
        Method method = routeDefine.getMethod();
        Parameter[] params = method.getParameters();
        Type[] types = method.getGenericParameterTypes();
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                if (ArrayUtil.contains(routeDefine.getRequestMethod(), RequestMethod.GET)) {
                    throw new UnsupportedOperationException("get 请求不支持 RequestBody");
                } else {
                    Type type = types[i];
                    if (httpRequest.body() == null) {
                        log.warn("request body为空");
                        routeDefine.getParams()[i] = null;
                    } else {
                        JavaType javaType = JsonTools.DEFAULT.getMapper().getTypeFactory().constructType(type);
                        routeDefine.getParams()[i] = JsonTools.DEFAULT.fromJson(httpRequest.body(), javaType);
                    }
                }
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String name = StrUtil.isBlank(requestParam.value()) ? routeDefine.getParamNames()[i] : requestParam.value();
                List<String> value = httpContext.getHttpRequest().parameters().get(name);
                if (value == null) {
                    log.info("缺少参数名为{}的值！设为null！", name);
                } else {
                    routeDefine.getParams()[i] = getRealValue(parameter, value);
                }
            } else if (parameter.isAnnotationPresent(PathParam.class)) {
                Map<String, String> map = PathMatcher.me.extractUriTemplateVariables(routeDefine.getPath(), httpRequest.url());
                routeDefine.getParams()[i] = Convert.convert(parameter.getType(), map.get(routeDefine.getParamNames()[i]));
            } else if (parameter.getType().isAssignableFrom(HttpRequest.class)) {
                routeDefine.getParams()[i] = httpRequest;
            } else if (parameter.getType().isAssignableFrom(HttpResponse.class)) {
                routeDefine.getParams()[i] = httpResponse;
            } else {
                routeDefine.getParams()[i] = null;
            }
        }

    }

    private Object getRealValue(Parameter parameter, List<String> value) {
        Class<?> parameterType = parameter.getType();
        if (Collection.class.isAssignableFrom(parameterType)) {
            return Convert.convert(parameter.getType(), value);
        } else {
            return Convert.convert(parameter.getType(), value.get(0));
        }
    }

    private CompletableFuture<HttpContext> invokeMethod(HttpContext httpContext, RouteDefine routeDefine) {
        return CompletableFuture.supplyAsync(() -> {
            Object result = null;
            Method method = routeDefine.getMethod();
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            try {
                result = method.invoke(routeDefine.getObject(), routeDefine.getParams());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            httpContext.getHttpResponse().data(result);
            return httpContext;
        }, executorService);
    }

}
