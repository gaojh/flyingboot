package com.github.flying.mvc.http;

import com.github.flying.mvc.interceptor.HandlerInterceptorChain;
import com.github.flying.mvc.utils.RespUtils;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.github.flying.context.ApplicationContext;
import com.github.flying.context.ApplicationUtil;
import com.github.flying.mvc.Mvcs;
import com.github.flying.mvc.utils.MimeTypeUtils;
import com.github.flying.server.context.HttpContext;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.*;

/**
 * @author 高建华
 * @date 2019-03-31 12:41
 */
public class FlyingHttpDispatcher implements HttpDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(FlyingHttpDispatcher.class);
    private HttpRouter httpRouter;
    private ApplicationContext applicationContext;
    /**
     * 页面缓存，默认1000条
     */
    private Cache<String, byte[]> cache = CacheUtil.newLFUCache(1000);

    public FlyingHttpDispatcher() {
        httpRouter = new FlyingHttpRouter();
        applicationContext = ApplicationUtil.getApplicationContext();
    }



    @Override
    public void doDispatcher(HttpContext httpContext) throws Exception {
        HttpRequest httpRequest = httpContext.getHttpRequest();
        HttpResponse httpResponse = httpContext.getHttpResponse();
        String url = httpRequest.url();
        CompletableFuture<HttpContext> future;
        HandlerInterceptorChain interceptorChain = new HandlerInterceptorChain(applicationContext.getInterceptor(url));

        if (StrUtil.contains(url, '.')) {
            //认为是静态资源
            httpResponse.success(true).data(getStaticResource(httpRequest.request(), url));
            future = CompletableFuture.completedFuture(httpContext);
        } else {
            if (interceptorChain.applyPreHandle(httpRequest, httpResponse)) {
                HttpRoute httpRoute = applicationContext.getHttpRoute(url);

                if (httpRoute == null) {
                    httpResponse.success(false).httpResponseStatus(HttpResponseStatus.BAD_REQUEST).msg("没有配置对应的路由：" + url);
                    future = CompletableFuture.completedFuture(httpContext);
                } else {
                    future = httpRouter.route(httpContext, httpRoute);
                }
            } else {
                httpResponse.success(false).msg("已拦截").httpResponseStatus(HttpResponseStatus.OK);
                future = CompletableFuture.completedFuture(httpContext);
            }
        }

        future.thenApply(context -> {
            try {
                interceptorChain.applyPostHandle(context.getHttpRequest(), context.getHttpResponse());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return context;
        }).thenAccept(RespUtils::sendResponse).thenRun(() -> {
            Mvcs.request.remove();
            Mvcs.response.remove();
            ReferenceCountUtil.release(httpRequest.request());
        });

    }

    private FullHttpResponse getStaticResource(FullHttpRequest request, String requestURI) {
        byte[] bytes = cache.get(requestURI);
        if (null == bytes) {
            InputStream is = this.getClass().getResourceAsStream("/public" + requestURI);
            if (is == null) {
                is = this.getClass().getResourceAsStream("/static" + requestURI);
            }
            bytes = IoUtil.readBytes(is);
            cache.put(requestURI, bytes);
        }


        if (bytes != null && bytes.length > 0) {
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));
            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, MimeTypeUtils.getContentType(requestURI));
            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
            resp.headers().add(HttpHeaderNames.CONTENT_LOCATION, request.headers().get("Host") + requestURI);
            return resp;
        } else {
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer("未找到你要的资源".getBytes(Charset.forName("UTF-8"))));
            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
            return resp;
        }

    }

}
