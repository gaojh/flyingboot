package com.github.gaojh.server.http;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.github.gaojh.mvc.interceptor.HandlerInterceptorChain;
import com.github.gaojh.mvc.interceptor.HandlerResponse;
import com.github.gaojh.mvc.interceptor.InterceptorContext;
import com.github.gaojh.mvc.route.SimpleRouteHandler;
import com.github.gaojh.mvc.route.RouteContext;
import com.github.gaojh.mvc.route.RouteDefine;
import com.github.gaojh.mvc.route.RouteHandler;
import com.github.gaojh.mvc.utils.MimeTypeUtils;
import com.github.gaojh.mvc.utils.RespUtils;
import com.github.gaojh.server.context.HttpContext;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author 高建华
 * @date 2019-03-31 12:41
 */
public class DefaultHttpDispatcher implements HttpDispatcher {

    private RouteHandler router;
    /**
     * 页面缓存，默认1000条
     */
    private Cache<String, byte[]> cache = CacheUtil.newLFUCache(1000);

    public DefaultHttpDispatcher() {
        router = new SimpleRouteHandler();
    }


    @Override
    public void doDispatcher(HttpContext httpContext) throws Exception {
        HttpRequest httpRequest = httpContext.getHttpRequest();
        HttpResponse httpResponse = httpContext.getHttpResponse();
        String url = httpRequest.url();
        CompletableFuture<HttpContext> future;
        HandlerInterceptorChain handlerInterceptorChain = new HandlerInterceptorChain(InterceptorContext.getInterceptor(url));

        //如果是反向代理模式，直接调用对应的route
        HandlerResponse handlerResponse = handlerInterceptorChain.applyPreHandle(httpRequest, httpResponse);
        if (handlerResponse.isSuccess()) {
            if (StrUtil.contains(url, '.')) {
                //认为是静态资源
                httpResponse.success(true).data(getStaticResource(httpRequest.request(), url));
                future = CompletableFuture.completedFuture(httpContext);
            } else if (StrUtil.equals(url, "/") || StrUtil.isBlank(url)) {
                httpResponse.success(true).data(getStaticResource(httpRequest.request(), "/index.html"));
                future = CompletableFuture.completedFuture(httpContext);
            } else {
                RouteDefine routeDefine = RouteContext.getRoute(url);
                if (routeDefine == null) {
                    httpResponse.success(false).httpResponseStatus(HttpResponseStatus.BAD_REQUEST).msg("没有配置对应的路由：" + url);
                    future = CompletableFuture.completedFuture(httpContext);
                } else {
                    future = router.invoke(httpContext, routeDefine);
                }
            }
        } else {
            httpResponse.success(false).msg(handlerResponse.getMsg()).httpResponseStatus(HttpResponseStatus.FORBIDDEN);
            future = CompletableFuture.completedFuture(httpContext);
        }

        future.thenApply(context -> {
            try {
                handlerInterceptorChain.applyPostHandle(context.getHttpRequest(), context.getHttpResponse());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return context;
        }).thenAccept(RespUtils::sendResponse).thenRun(() -> ReferenceCountUtil.release(httpRequest.request()));

    }

    private FullHttpResponse getStaticResource(FullHttpRequest request, String requesturi) {
        byte[] bytes = cache.get(requesturi);
        if (null == bytes) {
            InputStream is = this.getClass().getResourceAsStream("/public" + requesturi);
            if (is == null) {
                is = this.getClass().getResourceAsStream("/static" + requesturi);
            }

            if (is == null) {
                return notFoundResponse();
            }

            bytes = IoUtil.readBytes(is);
            cache.put(requesturi, bytes);
        }


        if (bytes != null && bytes.length > 0) {
            return foundResponse(bytes, request.headers().get("Host"), requesturi);
        } else {
            return notFoundResponse();
        }
    }

    private FullHttpResponse foundResponse(byte[] bytes, String host, String requesturi) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));
        resp.headers().set(HttpHeaderNames.CONTENT_TYPE, MimeTypeUtils.getContentType(requesturi));
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        resp.headers().add(HttpHeaderNames.CONTENT_LOCATION, host + requesturi);
        return resp;
    }

    private FullHttpResponse notFoundResponse() {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer("未找到你要的资源".getBytes(StandardCharsets.UTF_8)));
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return resp;
    }

}
