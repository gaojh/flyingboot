package com.github.gaojh.mvc.interceptor;

import com.github.gaojh.server.http.HttpRequest;
import com.github.gaojh.server.http.HttpResponse;

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
            addInterceptor(handlerInterceptor);
        }
    }

    public HandlerInterceptorChain(List<HandlerInterceptor> handlerInterceptors) {
        this.handlerInterceptorList.addAll(handlerInterceptors);
    }

    public void addInterceptor(HandlerInterceptor handlerInterceptor) {
        this.handlerInterceptorList.add(handlerInterceptor);
    }

    public void addInterceptors(List<HandlerInterceptor> handlerInterceptors) {
        this.handlerInterceptorList.addAll(handlerInterceptors);
    }

    public HandlerResponse applyPreHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        if (!handlerInterceptorList.isEmpty()) {
            for (int i = 0; i < handlerInterceptorList.size(); i++) {
                HandlerInterceptor handlerInterceptor = handlerInterceptorList.get(i);
                HandlerResponse handlerResponse = handlerInterceptor.preHandle(httpRequest, httpResponse);
                if (!handlerResponse.isSuccess()) {
                    handlerInterceptor.afterCompletion(httpRequest, httpResponse);
                    return handlerResponse;
                }
                this.interceptorIndex = i;
            }
        }
        return HandlerResponse.success();
    }

    public void applyPostHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        if (!handlerInterceptorList.isEmpty()) {
            for (int i = handlerInterceptorList.size() - 1; i >= 0; i--) {
                HandlerInterceptor handlerInterceptor = handlerInterceptorList.get(i);
                handlerInterceptor.postHandle(httpRequest, httpResponse);
            }
        }
    }

}
