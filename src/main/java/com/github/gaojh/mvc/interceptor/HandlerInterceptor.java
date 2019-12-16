package com.github.gaojh.mvc.interceptor;

import com.github.gaojh.server.http.HttpRequest;
import com.github.gaojh.server.http.HttpResponse;

/**
 * @author 高建华
 * @date 2019-01-13 21:36
 */
public interface HandlerInterceptor {

    /**
     * 前置处理
     * @param httpRequest
     * @param httpResponse
     * @return
     * @throws Exception
     */
    HandlerResponse preHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception;

    /**
     * 后置处理
     * @param httpRequest
     * @param httpResponse
     * @throws Exception
     */
    void postHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception;

}
