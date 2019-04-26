package com.github.flying.mvc.http;

import com.github.flying.server.context.HttpContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author 高建华
 * @date 2019-03-30 16:18
 */
public interface HttpRouter {

    /**
     * 路由处理
     *
     * @return
     * @throws Exception
     */
    CompletableFuture<HttpContext> route(HttpContext httpContext, HttpRoute httpRoute) throws Exception;
}
