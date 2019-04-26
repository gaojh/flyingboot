package com.github.flying.mvc.interceptor;

import com.github.flying.mvc.http.HttpRequest;
import com.github.flying.mvc.http.HttpResponse;

/**
 * @author 高建华
 * @date 2019-01-13 21:36
 */
public interface HandlerInterceptor {

    /**
     * 前置过滤
     * @param httpRequest
     * @param httpResponse
     * @return
     * @throws Exception
     */
    boolean preHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception;

    /**
     * 后置过滤
     * @param httpRequest
     * @param httpResponse
     * @throws Exception
     */
    void postHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception;

    /**
     * 最终过滤
     * @param httpRequest
     * @param httpResponse
     * @throws Exception
     */
    void afterCompletion(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception;
}
