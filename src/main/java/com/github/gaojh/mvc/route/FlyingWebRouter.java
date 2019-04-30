package com.github.gaojh.mvc.route;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.github.gaojh.context.ApplicationContext;
import com.github.gaojh.context.ApplicationUtil;
import com.github.gaojh.mvc.annotation.*;
import com.github.gaojh.mvc.http.HttpRequest;
import com.github.gaojh.mvc.http.HttpResponse;
import com.github.gaojh.mvc.utils.JsonTools;
import com.github.gaojh.mvc.utils.PathMatcher;
import com.github.gaojh.server.context.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FlyingWebRouter implements WebRouter {

    private static final Logger logger = LoggerFactory.getLogger(FlyingWebRouter.class);
    private ExecutorService executorService;

    @Override
    public CompletableFuture<HttpContext> invoke(HttpContext httpContext, WebRoute webRoute) throws Exception {
        ApplicationContext applicationContext = ApplicationUtil.getApplicationContext();
        this.executorService = applicationContext.getExecutorService();
        setParams(httpContext, webRoute);
        return invokeMethod(httpContext, webRoute);
    }


    private void setParams(HttpContext httpContext, WebRoute webRoute) {
        HttpRequest httpRequest = httpContext.getHttpRequest();
        HttpResponse httpResponse = httpContext.getHttpResponse();
        Method method = webRoute.getMethod();
        Parameter[] params = method.getParameters();
        Type[] types = method.getGenericParameterTypes();
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                if (ArrayUtil.contains(webRoute.getRequestMethod(), RequestMethod.GET)) {
                    throw new UnsupportedOperationException("get 请求不支持 RequestBody");
                } else {
                    Type type = types[i];
                    if (httpRequest.body() == null) {
                        logger.warn("request body为空");
                        webRoute.getParams()[i] = null;
                    } else {
                        JavaType javaType = JsonTools.DEFAULT.getMapper().getTypeFactory().constructType(type);
                        webRoute.getParams()[i] = JsonTools.DEFAULT.fromJson(httpRequest.body(), javaType);
                    }
                }
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String name = StrUtil.isBlank(requestParam.value()) ? webRoute.getParamNames()[i] : requestParam.value();
                List<String> value = httpContext.getHttpRequest().parameters().get(name);
                if (value == null) {
                    logger.info("缺少参数名为{}的值！设为null！", name);
                } else {
                    webRoute.getParams()[i] = getRealValue(parameter, value);
                }
            } else if (parameter.isAnnotationPresent(PathParam.class)) {
                Map<String, String> map = PathMatcher.me.extractUriTemplateVariables(webRoute.getUrlMapping(), httpRequest.url());
                webRoute.getParams()[i] = Convert.convert(parameter.getType(), map.get(webRoute.getParamNames()[i]));
            } else if (parameter.getType().isAssignableFrom(HttpRequest.class)) {
                webRoute.getParams()[i] = httpRequest;
            } else if (parameter.getType().isAssignableFrom(HttpResponse.class)) {
                webRoute.getParams()[i] = httpResponse;
            } else {
                webRoute.getParams()[i] = null;
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


    private CompletableFuture<HttpContext> invokeMethod(HttpContext httpContext, WebRoute webRoute) {
        return CompletableFuture.supplyAsync(() -> {
            Object result = ReflectUtil.invoke(webRoute.getObject(), webRoute.getMethod(), webRoute.getParams());
            httpContext.getHttpResponse().data(result);
            return httpContext;
        }, executorService);
    }

}
