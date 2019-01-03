package com.gao.flying.mvc.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author 高建华
 * @date 2018/7/5 上午11:18
 */
public interface FlyingResponse {

    ChannelHandlerContext ctx();

    FlyingResponse success(boolean res);

    boolean success();

    FlyingResponse msg(String msg);

    String msg();

    FlyingResponse data(Object data);

    Object data();

    FlyingResponse httpResponseStatus(HttpResponseStatus httpResponseStatus);

    HttpResponseStatus httpResponseStatus();
}
