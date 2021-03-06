package com.github.gaojh.server.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author 高建华
 * @date 2018/7/5 上午11:18
 */
public interface HttpResponse {

    HttpResponse success(boolean res);

    boolean success();

    HttpResponse msg(String msg);

    String msg();

    HttpResponse data(Object data);

    Object data();

    HttpResponse httpResponseStatus(HttpResponseStatus httpResponseStatus);

    HttpResponseStatus httpResponseStatus();
}
