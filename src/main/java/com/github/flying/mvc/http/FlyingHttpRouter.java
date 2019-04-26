package com.github.flying.mvc.http;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.github.flying.context.ApplicationContext;
import com.github.flying.context.ApplicationUtil;
import com.github.flying.mvc.annotation.PathParam;
import com.github.flying.mvc.annotation.RequestBody;
import com.github.flying.mvc.annotation.RequestMapping;
import com.github.flying.mvc.annotation.RequestParam;
import com.github.flying.mvc.utils.JsonTools;
import com.github.flying.mvc.utils.PathMatcher;
import com.github.flying.server.context.HttpContext;
import org.apache.commons.lang3.StringUtils;
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
public class FlyingHttpRouter implements HttpRouter {

    private static final Logger logger = LoggerFactory.getLogger(FlyingHttpRouter.class);
    private ExecutorService executorService;

    @Override
    public CompletableFuture<HttpContext> route(HttpContext httpContext, HttpRoute httpRoute) throws Exception {
        ApplicationContext applicationContext = ApplicationUtil.getApplicationContext();
        this.executorService = applicationContext.getExecutorService();
        setParams(httpContext, httpRoute);
        return invoke(httpContext, httpRoute);
    }


    private void setParams(HttpContext httpContext, HttpRoute httpRoute) {
        HttpRequest httpRequest = httpContext.getHttpRequest();
        HttpResponse httpResponse = httpContext.getHttpResponse();
        Method method = httpRoute.getMethod();
        Parameter[] params = method.getParameters();
        Type[] types = method.getGenericParameterTypes();
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                if (httpRoute.getHttpMethod().equals(RequestMapping.METHOD.GET)) {
                    throw new UnsupportedOperationException("get 请求不支持 RequestBody");
                } else {
                    Type type = types[i];
                    if (httpRequest.body() == null) {
                        logger.warn("request body为空");
                        httpRoute.getParams()[i] = null;
                    } else {
                        JavaType javaType = JsonTools.DEFAULT.getMapper().getTypeFactory().constructType(type);
                        httpRoute.getParams()[i] = JsonTools.DEFAULT.fromJson(httpRequest.body(), javaType);
                    }
                }
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String name = StringUtils.isBlank(requestParam.value()) ? httpRoute.getParamNames()[i] : requestParam.value();
                List<String> value = httpContext.getHttpRequest().parameters().get(name);
                if (value == null) {
                    logger.info("缺少参数名为{}的值！设为null！", name);
                } else {
                    httpRoute.getParams()[i] = getRealValue(parameter, value);
                }
            } else if (parameter.isAnnotationPresent(PathParam.class)) {
                Map<String, String> map = PathMatcher.me.extractUriTemplateVariables(httpRoute.getUrlMapping(), httpRequest.url());
                httpRoute.getParams()[i] = Convert.convert(parameter.getType(), map.get(httpRoute.getParamNames()[i]));
            } else if (parameter.getType().isAssignableFrom(HttpRequest.class)) {
                httpRoute.getParams()[i] = httpRequest;
            } else if (parameter.getType().isAssignableFrom(HttpResponse.class)) {
                httpRoute.getParams()[i] = httpResponse;
            } else {
                httpRoute.getParams()[i] = null;
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


    private CompletableFuture<HttpContext> invoke(HttpContext httpContext, HttpRoute httpRoute) {
        return CompletableFuture.supplyAsync(() -> {
            Object result = ReflectUtil.invoke(httpRoute.getObject(), httpRoute.getMethod(), httpRoute.getParams());
            httpContext.getHttpResponse().data(result);
            return httpContext;
        }, executorService);
    }

}
