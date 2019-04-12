package com.gao.flying.mvc.http;

import com.gao.flying.server.context.HttpContext;

/**
 * @author 高建华
 * @date 2019-03-30 16:32
 */
public interface HttpDispatcher {

    void doDispatcher(HttpContext httpContext) throws Exception;
}
