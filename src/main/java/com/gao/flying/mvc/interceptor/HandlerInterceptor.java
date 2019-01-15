package com.gao.flying.mvc.interceptor;

import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;

/**
 * @author 高建华
 * @date 2019-01-13 21:36
 */
public interface HandlerInterceptor {

    boolean preHandle(FlyingRequest flyingRequest, FlyingResponse flyingResponse) throws Exception;

    void postHandle(FlyingRequest flyingRequest, FlyingResponse flyingResponse) throws Exception;

    void afterCompletion(FlyingRequest flyingRequest, FlyingResponse flyingResponse) throws Exception;
}
