package com.github.flying.mvc.interceptor;

import com.github.flying.mvc.http.HttpRequest;
import com.github.flying.mvc.http.HttpResponse;

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

    public boolean applyPreHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        if (!handlerInterceptorList.isEmpty()) {
            for (int i = 0; i < handlerInterceptorList.size(); i++) {
                HandlerInterceptor handlerInterceptor = handlerInterceptorList.get(i);
                if (!handlerInterceptor.preHandle(httpRequest, httpResponse)) {
                    triggerAfterCompletion(httpRequest, httpResponse);
                    return false;
                }
                this.interceptorIndex = i;
            }
        }
        return true;
    }

    public void applyPostHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        if (!handlerInterceptorList.isEmpty()) {
            for (int i = handlerInterceptorList.size() - 1; i >= 0; i--) {
                HandlerInterceptor handlerInterceptor = handlerInterceptorList.get(i);
                handlerInterceptor.postHandle(httpRequest, httpResponse);
            }
        }
    }

    private void triggerAfterCompletion(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        if (!handlerInterceptorList.isEmpty()) {
            for (int i = this.interceptorIndex; i >= 0; i--) {
                HandlerInterceptor handlerInterceptor = handlerInterceptorList.get(i);
                handlerInterceptor.afterCompletion(httpRequest, httpResponse);
            }
        }
    }
}
