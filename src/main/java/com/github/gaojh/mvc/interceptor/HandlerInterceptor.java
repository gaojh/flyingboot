package com.github.gaojh.mvc.interceptor;

import com.github.gaojh.mvc.http.HttpRequest;
import com.github.gaojh.mvc.http.HttpResponse;

/**
 * @author 高建华
 * @date 2019-01-13 21:36
 */
public interface HandlerInterceptor {

    boolean preHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception;

    void postHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception;

    void afterCompletion(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception;
}
