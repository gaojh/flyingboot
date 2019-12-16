package com.github.gaojh.server.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author 高建华
 * @date 2018/6/24 下午8:26
 */
public class DefaultHttpResponse implements HttpResponse {

    private ChannelHandlerContext ctx;
    private boolean success;
    private String msg;
    private Object data;
    private HttpResponseStatus httpResponseStatus;

    public static HttpResponse buildSuccess(ChannelHandlerContext ctx) {
        DefaultHttpResponse response = new DefaultHttpResponse();
        response.ctx = ctx;
        response.success = true;
        response.httpResponseStatus = HttpResponseStatus.OK;
        return response;
    }

    @Override
    public HttpResponse success(boolean res) {
        this.success = res;
        return this;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public HttpResponse msg(String msg) {
        this.msg = msg;
        return this;
    }

    @Override
    public String msg() {
        return msg;
    }

    @Override
    public HttpResponse data(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public Object data() {
        return data;
    }

    @Override
    public HttpResponse httpResponseStatus(HttpResponseStatus httpResponseStatus) {
        this.httpResponseStatus = httpResponseStatus;
        return this;
    }

    @Override
    public HttpResponseStatus httpResponseStatus() {
        return this.httpResponseStatus;
    }

}
