package com.github.gaojh.mvc.route;

import com.github.gaojh.server.context.HttpContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author 高建华
 * @date 2019-03-30 16:18
 */
public interface RouteHandler {

    /**
     * 路由处理
     * @param httpContext http上下文
     * @param routeDefine 路由定义
     * @return
     * @throws Exception
     */
    CompletableFuture<HttpContext> invoke(HttpContext httpContext, RouteDefine routeDefine) throws Exception;
}
