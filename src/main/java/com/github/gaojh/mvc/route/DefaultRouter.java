package com.github.gaojh.mvc.route;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.github.gaojh.ioc.context.ApplicationContext;
import com.github.gaojh.ioc.context.ApplicationUtil;
import com.github.gaojh.mvc.annotation.*;
import com.github.gaojh.server.http.HttpRequest;
import com.github.gaojh.server.http.HttpResponse;
import com.github.gaojh.mvc.utils.JsonTools;
import com.github.gaojh.mvc.utils.PathMatcher;
import com.github.gaojh.server.context.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DefaultRouter implements Router {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRouter.class);
    private ExecutorService executorService;

    @Override
    public CompletableFuture<HttpContext> invoke(HttpContext httpContext, Route route) throws Exception {
        ApplicationContext applicationContext = ApplicationUtil.getApplicationContext();
        this.executorService = applicationContext.executorService();
        setParams(httpContext, route);
        return invokeMethod(httpContext, route);
    }


    private void setParams(HttpContext httpContext, Route route) {
        HttpRequest httpRequest = httpContext.getHttpRequest();
        HttpResponse httpResponse = httpContext.getHttpResponse();
        Method method = route.getMethod();
        Parameter[] params = method.getParameters();
        Type[] types = method.getGenericParameterTypes();
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                if (ArrayUtil.contains(route.getRequestMethod(), RequestMethod.GET)) {
                    throw new UnsupportedOperationException("get 请求不支持 RequestBody");
                } else {
                    Type type = types[i];
                    if (httpRequest.body() == null) {
                        logger.warn("request body为空");
                        route.getParams()[i] = null;
                    } else {
                        JavaType javaType = JsonTools.DEFAULT.getMapper().getTypeFactory().constructType(type);
                        route.getParams()[i] = JsonTools.DEFAULT.fromJson(httpRequest.body(), javaType);
                    }
                }
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String name = StrUtil.isBlank(requestParam.value()) ? route.getParamNames()[i] : requestParam.value();
                List<String> value = httpContext.getHttpRequest().parameters().get(name);
                if (value == null) {
                    logger.info("缺少参数名为{}的值！设为null！", name);
                } else {
                    route.getParams()[i] = getRealValue(parameter, value);
                }
            } else if (parameter.isAnnotationPresent(PathParam.class)) {
                Map<String, String> map = PathMatcher.me.extractUriTemplateVariables(route.getUrlMapping(), httpRequest.url());
                route.getParams()[i] = Convert.convert(parameter.getType(), map.get(route.getParamNames()[i]));
            } else if (parameter.getType().isAssignableFrom(HttpRequest.class)) {
                route.getParams()[i] = httpRequest;
            } else if (parameter.getType().isAssignableFrom(HttpResponse.class)) {
                route.getParams()[i] = httpResponse;
            } else {
                route.getParams()[i] = null;
            }
        }

    }

    private Object getRealValue(Parameter parameter, List<String> value) {
        Class parameterType = parameter.getType();
        if (Collection.class.isAssignableFrom(parameterType)) {
            return Convert.convert(parameter.getType(), value);
        } else {
            return Convert.convert(parameter.getType(), value.get(0));
        }
    }

    private CompletableFuture<HttpContext> invokeMethod(HttpContext httpContext, Route route) {
        return CompletableFuture.supplyAsync(() -> {
            Object result = null;
            try {
                result = route.getMethod().invoke(route.getObject(),route.getParams());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            httpContext.getHttpResponse().data(result);
            return httpContext;
        }, executorService);
    }

}
