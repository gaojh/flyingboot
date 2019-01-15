package com.gao.flying.mvc.interceptor;

import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 高建华
 * @date 2019-01-15 09:41
 */
public class HandlerInterceptorChain {


    private List<HandlerInterceptor> handlerInterceptorList = new ArrayList<>();
    private int interceptorIndex = -1;

    public HandlerInterceptorChain() {
        this((HandlerInterceptor) null);
    }

    public HandlerInterceptorChain(HandlerInterceptor... handlerInterceptors) {
        for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
            addIntercepter(handlerInterceptor);
        }
    }

    public HandlerInterceptorChain(List<HandlerInterceptor> handlerInterceptors) {
        this.handlerInterceptorList.addAll(handlerInterceptors);
    }

    public void addIntercepter(HandlerInterceptor handlerInterceptor) {
        this.handlerInterceptorList.add(handlerInterceptor);
    }

    public void addIntercepters(List<HandlerInterceptor> handlerInterceptors) {
        this.handlerInterceptorList.addAll(handlerInterceptors);
    }

    public boolean applyPreHandle(FlyingRequest flyingRequest, FlyingResponse flyingResponse) throws Exception {
        if (!handlerInterceptorList.isEmpty()) {
            for (int i = 0; i < handlerInterceptorList.size(); i++) {
                HandlerInterceptor handlerInterceptor = handlerInterceptorList.get(i);
                if (!handlerInterceptor.preHandle(flyingRequest, flyingResponse)) {
                    triggerAfterCompletion(flyingRequest, flyingResponse);
                    return false;
                }
                this.interceptorIndex = i;
            }
        }
        return true;
    }

    public void applyPostHandle(FlyingRequest flyingRequest, FlyingResponse flyingResponse) throws Exception {
        if (!handlerInterceptorList.isEmpty()) {
            for (int i = handlerInterceptorList.size() - 1; i >= 0; i--) {
                HandlerInterceptor handlerInterceptor = handlerInterceptorList.get(i);
                handlerInterceptor.postHandle(flyingRequest, flyingResponse);
            }
        }
    }

    private void triggerAfterCompletion(FlyingRequest flyingRequest, FlyingResponse flyingResponse) throws Exception {
        if (!handlerInterceptorList.isEmpty()) {
            for (int i = this.interceptorIndex; i >= 0; i--) {
                HandlerInterceptor handlerInterceptor = handlerInterceptorList.get(i);
                handlerInterceptor.afterCompletion(flyingRequest, flyingResponse);
            }
        }
    }
}
