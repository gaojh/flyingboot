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
import com.gao.flying.mvc.annotation.RequestBody;
import com.gao.flying.mvc.annotation.RequestParam;
import com.gao.flying.mvc.utils.ClassUtils;
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
import java.util.concurrent.CompletableFuture;

/**
 * @author 高建华
 * @date 2018/6/22 下午11:06
 */
public enum Dispatcher {
    me;
    private Cache<String, byte[]> cache = CacheUtil.newLFUCache(1000);
    private static final String IGNORE = "^.+\\.(html|bmp|jsp|png|gif|jpg|js|css|jspx|jpeg|swf|ico|json|woff2|woff|ttf|svg)$";
    private static final Log log = LogFactory.get();

    public void doDispathcer(ServerContext serverContext, Request request, Response response) {
        //先判断是否是静态资源，不过滤
        HttpMethod httpMethod = request.method();

        if (httpMethod.equals(HttpMethod.GET) && (isStaticResource(request.url()) || "/".equals(request.url()))) {
            HttpResponse resp = getStaticResource(request.httpRequest(), request.url());
            request.ctx().writeAndFlush(resp);
            return;
        }

        //再判断是否是配置了Controller
        if (serverContext.getRoute(request.url()) == null) {
            RespUtils.sendError(request, "请求无法响应：" + request.uri(), HttpResponseStatus.BAD_REQUEST);
            return;
        }

        CompletableFuture<Response> future = null;
        if (httpMethod.equals(HttpMethod.GET)) {
            try {
                future = doGet(serverContext, request, response);
            } catch (Exception e) {
                e.printStackTrace();
                log.log(Level.ERROR, e, "调用Get方法出现异常");
            }
        } else if (httpMethod.equals(HttpMethod.POST)) {
            try {
                future = doPost(serverContext, request, response);
            } catch (Exception e) {
                log.log(Level.ERROR, e, "调用Post方法出现异常");
            }
        } else {
            RespUtils.sendError(request, "不支持该method", HttpResponseStatus.BAD_REQUEST);
        }

        if (future != null) {
            future.thenApply(resp -> {
                try {
                    if (resp.success()) {
                        RespUtils.sendResp(request, resp.data());
                    } else {
                        RespUtils.sendError(request, response.exception().getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    RespUtils.sendError(request, ex.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                } finally {
                    ReferenceCountUtil.release(request.httpRequest());
                }
                return resp;
            }).exceptionally(ex -> {
                try {
                    ex.printStackTrace();
                    RespUtils.sendError(request, ex.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    return response;
                } finally {
                    ReferenceCountUtil.release(request.httpRequest());
                }
            });
        } else {
            ReferenceCountUtil.release(request.httpRequest());
            RespUtils.sendError(request, "系统内部错误", HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private CompletableFuture<Response> doGet(ServerContext serverContext, Request request, Response response) {
        Route route = serverContext.getRoute(request.url());
        Method method = route.getMethod();
        Parameter[] params = method.getParameters();
        String[] parameterNames = ClassUtils.getMethodParamNames(method);

        Object[] values = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String name = StringUtils.isBlank(requestParam.value()) ? parameterNames[i] : requestParam.value();
                List<String> value = request.parameters().get(name);
                if (value == null) {
                    return CompletableFuture.completedFuture(response.success(false).exception(new IllegalArgumentException("缺少参数名为" + name + "的值！")));
                }
                values[i] = getRealValue(parameter, value);
            } else if ("com.gao.flying.mvc.http.Request".equals(parameter.getType().getName())) {
                values[i] = request;
            } else if ("com.gao.flying.mvc.http.Response".equals(parameter.getType().getName())) {
                values[i] = response;
            } else {
                values[i] = "null";
            }
        }

        return invoke(serverContext, response, route, method, values);

    }

    private CompletableFuture<Response> doPost(ServerContext serverContext, Request request, Response response) {
        Route route = serverContext.getRoute(request.url());
        Method method = route.getMethod();
        Parameter[] params = method.getParameters();
        Type[] types = method.getGenericParameterTypes();
        Object values[] = new Object[params.length];
        String[] parameterNames = ClassUtils.getMethodParamNames(method);

        if (request.isJsonRequest()) {
            for (int i = 0; i < params.length; i++) {
                Parameter parameter = params[i];
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                if (requestBody != null) {
                    //json转换为此对象
                    Type type = types[i];
                    if (request.body() == null) {
                        return CompletableFuture.completedFuture(response.success(false).exception(new RuntimeException(type.getTypeName() + "入参为空！")));
                    }

                    if (StringUtils.contains(type.getTypeName(), "<")) {
                        if (JSONUtil.isJsonArray(request.body())) {
                            try {
                                values[i] = JSONArray.parseArray(request.body(), type.getClass());
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(response.success(false).exception(e));
                            }
                        } else {
                            try {
                                values[i] = JSON.parseObject(request.body(), type);
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(response.success(false).exception(e));
                            }
                        }
                    } else {
                        if (JSONUtil.isJsonArray(request.body())) {
                            try {
                                values[i] = JSONArray.parseArray(request.body(), parameter.getType());
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(response.success(false).exception(e));
                            }
                        } else {
                            try {
                                values[i] = JSONArray.parseObject(request.body(), parameter.getType());
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(response.success(false).exception(e));
                            }
                        }
                    }
                } else if (requestParam != null) {
                    String name = StringUtils.isBlank(requestParam.value()) ? parameterNames[i] : requestParam.value();
                    List<String> value = request.parameters().get(name);
                    if (value == null) {
                        return CompletableFuture.completedFuture(response.success(false).exception(new IllegalArgumentException("缺少参数名为" + name + "的值！")));
                    }
                    values[i] = getRealValue(parameter, value);
                } else if ("com.gao.flying.mvc.http.Request".equals(parameter.getType().getName())) {
                    values[i] = request;
                } else if ("com.gao.flying.mvc.http.Response".equals(parameter.getType().getName())) {
                    values[i] = response;
                } else {
                    values[i] = "null";
                }
            }
        } else {
            for (int i = 0; i < params.length; i++) {
                Parameter parameter = params[i];
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                if (requestParam != null) {
                    String name = StringUtils.isBlank(requestParam.value()) ? parameterNames[i] : requestParam.value();
                    List<String> value = request.parameters().get(name);
                    if (value == null) {
                        return CompletableFuture.completedFuture(response.success(false).exception(new IllegalArgumentException("缺少参数名为" + name + "的值！")));
                    }
                    values[i] = getRealValue(parameter, value);
                } else if ("com.gao.flying.mvc.http.Request".equals(parameter.getType().getName())) {
                    values[i] = request;
                } else if ("com.gao.flying.mvc.http.Response".equals(parameter.getType().getName())) {
                    values[i] = response;
                } else {
                    values[i] = "null";
                }
            }
        }

        return invoke(serverContext, response, route, method, values);
    }

    private CompletableFuture<Response> invoke(ServerContext serverContext, Response response, Route config, Method method, Object[] values) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object result = method.invoke(config.getObject(), values);
                response.data(result);
            } catch (Exception e) {
                e.printStackTrace();
                response.exception(e).msg(e.getMessage()).success(false);
                return response;
            }
            return response;
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
