package com.gao.flying.mvc.http;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.log.level.Level;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.gao.flying.context.ServerContext;
import com.gao.flying.mvc.annotation.PathParam;
import com.gao.flying.mvc.annotation.RequestBody;
import com.gao.flying.mvc.annotation.RequestParam;
import com.gao.flying.mvc.utils.ClassUtils;
import com.gao.flying.mvc.utils.PathMatcher;
import com.gao.flying.mvc.utils.RespUtils;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author 高建华
 * @date 2018/6/22 下午11:06
 */
public enum Dispatcher {
    /**
     *
     */
    me;
    private Cache<String, byte[]> cache = CacheUtil.newLFUCache(1000);
    private static final String IGNORE = "^.+\\.(html|bmp|jsp|png|gif|jpg|js|css|jspx|jpeg|swf|ico|json|woff2|woff|ttf|svg|woff)$";
    private static final Log log = LogFactory.get();

    public void doDispathcer(ServerContext serverContext, FlyingRequest flyingRequest, FlyingResponse flyingResponse) {
        //先判断是否是静态资源，不过滤
        HttpMethod httpMethod = flyingRequest.method();

        if (httpMethod.equals(HttpMethod.GET) && (isStaticResource(flyingRequest.url()) || "/".equals(flyingRequest.url()))) {
            HttpResponse resp = getStaticResource(flyingRequest.httpRequest(), flyingRequest.url());
            flyingRequest.ctx().writeAndFlush(resp);
            return;
        }

        CompletableFuture<FlyingResponse> future = null;
        if (httpMethod.equals(HttpMethod.GET)) {
            try {
                future = doGet(serverContext, flyingRequest, flyingResponse);
            } catch (Exception e) {
                e.printStackTrace();
                log.log(Level.ERROR, e, "调用Get方法出现异常");
            }
        } else if (httpMethod.equals(HttpMethod.POST)) {
            try {
                future = doPost(serverContext, flyingRequest, flyingResponse);
            } catch (Exception e) {
                log.log(Level.ERROR, e, "调用Post方法出现异常");
            }
        } else {
            RespUtils.sendError(flyingRequest, "不支持该method", HttpResponseStatus.BAD_REQUEST);
        }

        if (future != null) {
            future.thenApply(resp -> {
                try {

                    if (resp.success()) {
                        RespUtils.sendResp(flyingRequest, resp.data());
                    } else {
                        RespUtils.sendError(flyingRequest, resp.msg(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    RespUtils.sendError(flyingRequest, ex.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                } finally {
                    ReferenceCountUtil.release(flyingRequest.httpRequest());
                }
                return resp;
            }).exceptionally(ex -> {
                try {
                    ex.printStackTrace();
                    RespUtils.sendError(flyingRequest, ex.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    return flyingResponse;
                } finally {
                    ReferenceCountUtil.release(flyingRequest.httpRequest());
                }
            });
        } else {
            ReferenceCountUtil.release(flyingRequest.httpRequest());
            RespUtils.sendError(flyingRequest, "系统内部错误", HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private CompletableFuture<FlyingResponse> doGet(ServerContext serverContext, FlyingRequest flyingRequest, FlyingResponse flyingResponse) {
        FlyingRoute flyingRoute = serverContext.fetchGetRoute(flyingRequest.url());
        if (flyingRoute == null) {
            return CompletableFuture.completedFuture(DefaultFlyingResponse.buildResponse().success(false).msg("未找到对应的处理器"));
        }
        Method method = flyingRoute.getMethod();
        Parameter[] params = method.getParameters();
        String[] parameterNames = ClassUtils.getMethodParamNames(method);

        Object[] values = new Object[params.length];
        setParamsValue(flyingRequest, flyingResponse, flyingRoute, params, parameterNames, values);

        return invoke(serverContext, flyingResponse, flyingRoute, method, values);

    }

    private void setParamsValue(FlyingRequest flyingRequest, FlyingResponse flyingResponse, FlyingRoute flyingRoute, Parameter[] params, String[] parameterNames, Object[] values) {
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            PathParam pathParam = parameter.getAnnotation(PathParam.class);
            setValue(flyingRequest, flyingResponse, flyingRoute, parameterNames, values, i, parameter, requestParam, pathParam);
        }
    }

    private void setValue(FlyingRequest flyingRequest, FlyingResponse flyingResponse, FlyingRoute flyingRoute, String[] parameterNames, Object[] values, int i, Parameter parameter, RequestParam requestParam, PathParam pathParam) {
        if (requestParam != null) {
            String name = StringUtils.isBlank(requestParam.value()) ? parameterNames[i] : requestParam.value();
            List<String> value = flyingRequest.parameters().get(name);
            if (value == null) {
                log.info("缺少参数名为{}的值！设为null！", name);
            } else {
                values[i] = getRealValue(parameter, value);
            }
        } else if (parameter.getType().isAssignableFrom(FlyingRequest.class)) {
            values[i] = flyingRequest;
        } else if (parameter.getType().isAssignableFrom(FlyingResponse.class)) {
            values[i] = flyingResponse;
        } else if (pathParam != null) {
            Map<String, String> map = PathMatcher.me.extractUriTemplateVariables(flyingRoute.getUrlMapping(), flyingRequest.url());
            values[i] = Convert.convert(parameter.getType(), map.get(parameterNames[i]));
        } else {
            values[i] = "null";
        }
    }

    private CompletableFuture<FlyingResponse> doPost(ServerContext serverContext, FlyingRequest flyingRequest, FlyingResponse flyingResponse) {
        FlyingRoute flyingRoute = serverContext.fetchPostRoute(flyingRequest.url());
        if (flyingRoute == null) {
            return CompletableFuture.completedFuture(DefaultFlyingResponse.buildResponse().success(false).msg("未找到对应的处理器"));
        }
        Method method = flyingRoute.getMethod();
        Parameter[] params = method.getParameters();
        Type[] types = method.getGenericParameterTypes();
        Object[] values = new Object[params.length];
        String[] parameterNames = ClassUtils.getMethodParamNames(method);

        if (flyingRequest.isJsonRequest()) {
            for (int i = 0; i < params.length; i++) {
                Parameter parameter = params[i];
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                PathParam pathParam = parameter.getAnnotation(PathParam.class);
                if (requestBody != null) {
                    //json转换为此对象
                    Type type = types[i];
                    if (flyingRequest.body() == null) {
                        return CompletableFuture.completedFuture(flyingResponse.success(false).exception(new RuntimeException(type.getTypeName() + "入参为空！")));
                    }

                    if (StringUtils.contains(type.getTypeName(), "<")) {
                        if (JSONUtil.isJsonArray(flyingRequest.body())) {
                            try {
                                values[i] = JSONArray.parseArray(flyingRequest.body(), type.getClass());
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(flyingResponse.success(false).exception(e));
                            }
                        } else {
                            try {
                                values[i] = JSON.parseObject(flyingRequest.body(), type);
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(flyingResponse.success(false).exception(e));
                            }
                        }
                    } else {
                        if (JSONUtil.isJsonArray(flyingRequest.body())) {
                            try {
                                values[i] = JSONArray.parseArray(flyingRequest.body(), parameter.getType());
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(flyingResponse.success(false).exception(e));
                            }
                        } else {
                            try {
                                values[i] = JSONArray.parseObject(flyingRequest.body(), parameter.getType());
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(flyingResponse.success(false).exception(e));
                            }
                        }
                    }
                } else {
                    setValue(flyingRequest, flyingResponse, flyingRoute, parameterNames, values, i, parameter, requestParam, pathParam);
                }
            }
        } else {
            setParamsValue(flyingRequest, flyingResponse, flyingRoute, params, parameterNames, values);
        }

        return invoke(serverContext, flyingResponse, flyingRoute, method, values);
    }

    private CompletableFuture<FlyingResponse> invoke(ServerContext serverContext, FlyingResponse flyingResponse, FlyingRoute config, Method method, Object[] values) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object result = method.invoke(config.getObject(), values);
                flyingResponse.data(result);
            } catch (Exception e) {
                e.printStackTrace();
                flyingResponse.exception(e).msg(e.getCause().getMessage()).success(false);
                return flyingResponse;
            }
            return flyingResponse;
        }, serverContext.getExecutorService());
    }

    private boolean isStaticResource(String requestURI) {
        return ReUtil.isMatch(IGNORE, requestURI);
    }


    private Object getRealValue(Parameter parameter, List<String> value) {
        if (StringUtils.containsAny(parameter.getType().getSimpleName(), "List", "ArrayList", "LinkedList")) {
            return Convert.convert(parameter.getType(), value);
        } else {
            return Convert.convert(parameter.getType(), value.get(0));
        }
    }


    private HttpResponse getStaticResource(FullHttpRequest request, String requestURI) {
        if (null == requestURI || requestURI.isEmpty() || "/".equals(requestURI)) {
            requestURI = "/index.html";
        }


        byte[] bytes = cache.get(requestURI);
        if (null == bytes) {
            InputStream is = this.getClass().getResourceAsStream("/public" + requestURI);
            if (is == null) {
                is = this.getClass().getResourceAsStream("/static" + requestURI);
            }
            bytes = inputStreamToBytes(is);
            cache.put(requestURI, bytes);
        }


        if (bytes != null && bytes.length > 0) {
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));
            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, getContentType(requestURI));
            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
            resp.headers().add(HttpHeaderNames.CONTENT_LOCATION, request.headers().get("Host") + requestURI);
            return resp;
        } else {
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("未找到你要的资源".getBytes(Charset.forName("UTF-8"))));
            resp.setStatus(HttpResponseStatus.NOT_FOUND);
            resp.headers().set("Content-Length", 0);
            return resp;
        }

    }


    private byte[] inputStreamToBytes(InputStream is) {
        ByteArrayOutputStream baos = null;
        try {
            if (null != is && is.available() > 0) {
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int num;
                while ((num = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, num);
                }
                return baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (null != baos) {
                    baos.close();
                }
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private String getContentType(String url) {
        if (StringUtils.contains(url, ".")) {
            String ext = StringUtils.substringAfterLast(url, ".");
            if ("html".equalsIgnoreCase(ext)) {
                return "text/html; charset=UTF-8";
            } else if ("ico".equalsIgnoreCase(ext)) {
                return "image/x-icon";
            } else if ("jpeg".equalsIgnoreCase(ext)) {
                return "image/jpeg";
            } else if ("jpg".equalsIgnoreCase(ext)) {
                return "image/jpeg";
            } else if ("pdf".equalsIgnoreCase(ext)) {
                return "application/pdf";
            } else if ("png".equalsIgnoreCase(ext)) {
                return "image/png";
            } else if ("css".equalsIgnoreCase(ext)) {
                return "text/css";
            } else if ("js".equalsIgnoreCase(ext)) {
                return "application/x-javascript";
            } else if ("gif".equalsIgnoreCase(ext)) {
                return "image/gif";
            } else if ("bmp".equalsIgnoreCase(ext)) {
                return "application/x-bmp";
            } else if ("svg".equalsIgnoreCase(ext)) {
                return "text/xml";
            } else if ("xml".equalsIgnoreCase(ext)) {
                return "text/xml";
            } else {
                return "text/plain; charset=UTF-8";
            }
        } else {
            return "text/plain; charset=UTF-8";
        }
    }

}
