package com.gao.flying.mvc.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author 高建华
 * @date 2018/6/24 下午8:26
 */
public class DefaultFlyingResponse implements FlyingResponse {

    private ChannelHandlerContext ctx;
    private boolean success;
    private String msg;
    private Object data;
    private HttpResponseStatus httpResponseStatus;

    public static FlyingResponse buildSuccess(ChannelHandlerContext ctx) {
        DefaultFlyingResponse response = new DefaultFlyingResponse();
        response.ctx = ctx;
        response.success = true;
        response.httpResponseStatus = HttpResponseStatus.OK;
        return response;
    }

    @Override
    public ChannelHandlerContext ctx() {
        return ctx;
    }

    @Override
    public FlyingResponse success(boolean res) {
        this.success = res;
        return this;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public FlyingResponse msg(String msg) {
        this.msg = msg;
        return this;
    }

    @Override
    public String msg() {
        return msg;
    }

    @Override
    public FlyingResponse data(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public Object data() {
        return data;
    }

    @Override
    public FlyingResponse httpResponseStatus(HttpResponseStatus httpResponseStatus) {
        this.httpResponseStatus = httpResponseStatus;
        return this;
    }

    @Override
    public HttpResponseStatus httpResponseStatus() {
        return this.httpResponseStatus;
    }

}
