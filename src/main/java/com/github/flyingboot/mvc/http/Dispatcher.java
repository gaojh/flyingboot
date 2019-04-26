//package com.gao.flyingboot.mvc.http;
//
//import cn.hutool.cache.Cache;
//import cn.hutool.cache.CacheUtil;
//import cn.hutool.core.convert.Convert;
//import cn.hutool.core.util.ReUtil;
//import cn.hutool.log.Log;
//import cn.hutool.log.LogFactory;
//import com.fasterxml.jackson.databind.JavaType;
//import Mvcs;
//import PathParam;
//import RequestBody;
//import RequestParam;
//import HandlerInterceptorChain;
//import ClassUtils;
//import JsonTools;
//import PathMatcher;
//import RespUtils;
//import io.netty.buffer.Unpooled;
//import io.netty.handler.codec.http.*;
//import io.netty.util.ReferenceCountUtil;
//import org.apache.commons.lang3.StringUtils;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Method;
//import java.lang.reflect.Parameter;
//import java.lang.reflect.Type;
//import java.nio.charset.Charset;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
///**
// * @author 高建华
// * @date 2018/6/22 下午11:06
// */
//public enum Dispatcher {
//    /**
//     *
//     */
//    me;
//    private Cache<String, byte[]> cache = CacheUtil.newLFUCache(1000);
//    private static final String IGNORE = "^.+\\.(html|bmp|jsp|png|gif|jpg|js|css|jspx|jpeg|swf|ico|json|woff2|woff|ttf|svg|woff)$";
//    private static final Log log = LogFactory.get();
//
//
//    public void execute(FlyingContext flyingContext, HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
//        Mvcs.request.set(httpRequest);
//        Mvcs.response.set(httpResponse);
//        HandlerInterceptorChain interceptorChain = new HandlerInterceptorChain(flyingContext.getInterceptors(httpRequest.url()));
//        CompletableFuture<HttpResponse> future;
//        RespUtils.sendResponse(httpRequest, httpResponse);
//
//        //拦截
//        if (interceptorChain.applyPreHandle(httpRequest, httpResponse)) {
//            HttpMethod httpMethod = httpRequest.method();
//            if (httpMethod.equals(HttpMethod.GET)) {
//                //是否是静态资源
//                if (isStaticResource(httpRequest.url()) || "/".equals(httpRequest.url())) {
//                    FullHttpResponse resp = getStaticResource(httpRequest.request(), httpRequest.url());
//                    future = CompletableFuture.completedFuture(httpResponse.success(true).data(resp));
//                } else {
//                    future = doGet(flyingContext, httpRequest, httpResponse);
//                }
//            } else if (httpMethod.equals(HttpMethod.POST)) {
//                future = doPost(flyingContext, httpRequest, httpResponse);
//            } else {
//                future = CompletableFuture.completedFuture(httpResponse.success(false).msg("不支持该method").httpResponseStatus(HttpResponseStatus.BAD_REQUEST));
//            }
//        } else {
//            future = CompletableFuture.completedFuture(httpResponse.success(false).msg("已拦截").httpResponseStatus(HttpResponseStatus.OK));
//        }
//
//        future.thenApply(resp -> {
//            try {
//                interceptorChain.applyPostHandle(httpRequest, resp);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return resp;
//        }).thenAccept(resp -> RespUtils.sendResponse(httpRequest, resp)).thenRun(() -> {
//            ReferenceCountUtil.release(httpRequest.request());
//            Mvcs.request.remove();
//            Mvcs.response.remove();
//        });
//
//
//    }
//
//    private CompletableFuture<HttpResponse> doGet(FlyingContext flyingContext, HttpRequest httpRequest, HttpResponse httpResponse) {
//        HttpRoute httpRoute = flyingContext.fetchGetRoute(httpRequest.url());
//        Method method = httpRoute.getMethod();
//        Parameter[] params = method.getParameters();
//        String[] parameterNames = ClassUtils.getMethodParamNames(method);
//
//        Object[] values = new Object[params.length];
//        setParamsValue(httpRequest, httpResponse, httpRoute, params, parameterNames, values);
//
//        return invoke(flyingContext, httpResponse, httpRoute, method, values);
//
//    }
//
//    private void setParamsValue(HttpRequest httpRequest, HttpResponse httpResponse, HttpRoute httpRoute, Parameter[] params, String[] parameterNames, Object[] values) {
//        for (int i = 0; i < params.length; i++) {
//            Parameter parameter = params[i];
//            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
//            PathParam pathParam = parameter.getAnnotation(PathParam.class);
//            setValue(httpRequest, httpResponse, httpRoute, parameterNames, values, i, parameter, requestParam, pathParam);
//        }
//    }
//
//    private void setValue(HttpRequest httpRequest, HttpResponse httpResponse, HttpRoute httpRoute, String[] parameterNames, Object[] values, int i, Parameter parameter, RequestParam requestParam, PathParam pathParam) {
//        if (requestParam != null) {
//            String name = StringUtils.isBlank(requestParam.value()) ? parameterNames[i] : requestParam.value();
//            List<String> value = httpRequest.parameters().get(name);
//            if (value == null) {
//                log.info("缺少参数名为{}的值！设为null！", name);
//            } else {
//                values[i] = getRealValue(parameter, value);
//            }
//        } else if (parameter.getType().isAssignableFrom(HttpRequest.class)) {
//            values[i] = httpRequest;
//        } else if (parameter.getType().isAssignableFrom(HttpResponse.class)) {
//            values[i] = httpResponse;
//        } else if (pathParam != null) {
//            Map<String, String> map = PathMatcher.me.extractUriTemplateVariables(httpRoute.getUrlMapping(), httpRequest.url());
//            values[i] = Convert.convert(parameter.getType(), map.get(parameterNames[i]));
//        } else {
//            values[i] = "null";
//        }
//    }
//
//    private CompletableFuture<HttpResponse> doPost(FlyingContext flyingContext, HttpRequest httpRequest, HttpResponse httpResponse) {
//        HttpRoute httpRoute = flyingContext.fetchPostRoute(httpRequest.url());
//        Method method = httpRoute.getMethod();
//        Parameter[] params = method.getParameters();
//        Type[] types = method.getGenericParameterTypes();
//        Object[] values = new Object[params.length];
//        String[] parameterNames = ClassUtils.getMethodParamNames(method);
//
//        if (httpRequest.isJsonRequest()) {
//            for (int i = 0; i < params.length; i++) {
//                Parameter parameter = params[i];
//                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
//                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
//                PathParam pathParam = parameter.getAnnotation(PathParam.class);
//                if (requestBody != null) {
//                    //json转换为此对象
//                    Type type = types[i];
//                    if (httpRequest.body() == null) {
//                        return CompletableFuture.completedFuture(httpResponse.success(false).msg("入参为空").httpResponseStatus(HttpResponseStatus.BAD_REQUEST));
//                    }
//                    JavaType javaType = JsonTools.DEFAULT.getMapper().getTypeFactory().constructType(type);
//                    values[i] = JsonTools.DEFAULT.fromJson(httpRequest.body(), javaType);
//                } else {
//                    setValue(httpRequest, httpResponse, httpRoute, parameterNames, values, i, parameter, requestParam, pathParam);
//                }
//            }
//        } else {
//            setParamsValue(httpRequest, httpResponse, httpRoute, params, parameterNames, values);
//        }
//
//        return invoke(flyingContext, httpResponse, httpRoute, method, values);
//    }
//
//    private CompletableFuture<HttpResponse> invoke(FlyingContext flyingContext, HttpResponse httpResponse, HttpRoute config, Method method, Object[] values) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                Object result = method.invoke(config.getObject(), values);
//                httpResponse.data(result);
//            } catch (Exception e) {
//                log.error(e);
//                httpResponse.msg(e.getCause().getMessage()).success(false);
//            }
//            return httpResponse;
//        }, flyingContext.getExecutorService());
//    }
//
//    private boolean isStaticResource(String requestURI) {
//        return ReUtil.isMatch(IGNORE, requestURI);
//    }
//
//
//    private Object getRealValue(Parameter parameter, List<String> value) {
//        Class parameterType = parameter.getType();
//        if (Collection.class.isAssignableFrom(parameterType)) {
//            return Convert.convert(parameter.getType(), value);
//        } else {
//            return Convert.convert(parameter.getType(), value.get(0));
//        }
//    }
//
//
//    private FullHttpResponse getStaticResource(FullHttpRequest request, String requestURI) {
//        if (null == requestURI || requestURI.isEmpty() || "/".equals(requestURI)) {
//            requestURI = "/index.html";
//        }
//
//
//        byte[] bytes = cache.get(requestURI);
//        if (null == bytes) {
//            InputStream is = this.getClass().getResourceAsStream("/public" + requestURI);
//            if (is == null) {
//                is = this.getClass().getResourceAsStream("/static" + requestURI);
//            }
//            bytes = inputStreamToBytes(is);
//            cache.put(requestURI, bytes);
//        }
//
//
//        if (bytes != null && bytes.length > 0) {
//            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));
//            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, getContentType(requestURI));
//            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
//            resp.headers().add(HttpHeaderNames.CONTENT_LOCATION, request.headers().get("Host") + requestURI);
//            return resp;
//        } else {
//            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("未找到你要的资源".getBytes(Charset.forName("UTF-8"))));
//            resp.setStatus(HttpResponseStatus.NOT_FOUND);
//            resp.headers().set("Content-Length", 0);
//            return resp;
//        }
//
//    }
//
//
//    private byte[] inputStreamToBytes(InputStream is) {
//        ByteArrayOutputStream baos = null;
//        try {
//            if (null != is && is.available() > 0) {
//                baos = new ByteArrayOutputStream();
//                byte[] buffer = new byte[1024];
//                int num;
//                while ((num = is.read(buffer)) != -1) {
//                    baos.write(buffer, 0, num);
//                }
//                return baos.toByteArray();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            try {
//                if (null != baos) {
//                    baos.close();
//                }
//                if (null != is) {
//                    is.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//
//    private String getContentType(String url) {
//        if (StringUtils.contains(url, ".")) {
//            String ext = StringUtils.substringAfterLast(url, ".");
//            if ("html".equalsIgnoreCase(ext)) {
//                return "text/html; charset=UTF-8";
//            } else if ("ico".equalsIgnoreCase(ext)) {
//                return "image/x-icon";
//            } else if ("jpeg".equalsIgnoreCase(ext)) {
//                return "image/jpeg";
//            } else if ("jpg".equalsIgnoreCase(ext)) {
//                return "image/jpeg";
//            } else if ("pdf".equalsIgnoreCase(ext)) {
//                return "application/pdf";
//            } else if ("png".equalsIgnoreCase(ext)) {
//                return "image/png";
//            } else if ("css".equalsIgnoreCase(ext)) {
//                return "text/css";
//            } else if ("js".equalsIgnoreCase(ext)) {
//                return "application/x-javascript";
//            } else if ("gif".equalsIgnoreCase(ext)) {
//                return "image/gif";
//            } else if ("bmp".equalsIgnoreCase(ext)) {
//                return "application/x-bmp";
//            } else if ("svg".equalsIgnoreCase(ext)) {
//                return "text/xml";
//            } else if ("xml".equalsIgnoreCase(ext)) {
//                return "text/xml";
//            } else {
//                return "text/plain; charset=UTF-8";
//            }
//        } else {
//            return "text/plain; charset=UTF-8";
//        }
//    }
//
//}
