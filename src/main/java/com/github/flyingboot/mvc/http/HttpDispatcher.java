package com.github.flyingboot.mvc.http;

import com.github.flyingboot.server.context.HttpContext;

/**
 * @author 高建华
 * @date 2019-03-30 16:32
 */
public interface HttpDispatcher {

    void doDispatcher(HttpContext httpContext) throws Exception;
}
