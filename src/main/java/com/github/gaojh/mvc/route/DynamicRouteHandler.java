package com.github.gaojh.mvc.route;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 动态路由处理器
 *
 * @author 高建华
 * @date 2019-04-30 09:38
 */
public interface DynamicRouteHandler {

    Object handle(FullHttpRequest fullHttpRequest);
}
