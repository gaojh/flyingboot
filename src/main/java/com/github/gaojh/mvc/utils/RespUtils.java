package com.github.gaojh.mvc.utils;

import com.github.gaojh.server.http.HttpRequest;
import com.github.gaojh.server.http.HttpResponse;
import com.github.gaojh.server.context.HttpContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

/**
 * @author 高建华
 * @date 2018/6/24 下午10:52
 */
public class RespUtils {

    public static void sendResponse(HttpContext httpContext) {
        HttpResponse response = httpContext.getHttpResponse();
        if (response.success()) {
            if (HttpUtil.isKeepAlive(httpContext.getHttpRequest().request())) {
                httpContext.getCtx().writeAndFlush(buildHttpResponse(response.data(), HttpResponseStatus.OK));
            } else {
                httpContext.getCtx().writeAndFlush(buildHttpResponse(response.data(), HttpResponseStatus.OK)).addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            if (HttpUtil.isKeepAlive(httpContext.getHttpRequest().request())) {
                httpContext.getCtx().writeAndFlush(buildHttpResponse(response.msg(), response.httpResponseStatus()));
            } else {
                httpContext.getCtx().writeAndFlush(buildHttpResponse(response.msg(), response.httpResponseStatus())).addListener(ChannelFutureListener.CLOSE);
            }
        }

    }

    public static void sendResponse(HttpRequest httpRequest, HttpResponse flyingResponse) {
        io.netty.handler.codec.http.HttpResponse httpResponse;
        if (flyingResponse.success()) {
            httpResponse = buildHttpResponse(flyingResponse.data(), HttpResponseStatus.OK);
        } else {
            httpResponse = buildHttpResponse(flyingResponse.msg(), flyingResponse.httpResponseStatus());
        }

        if (HttpUtil.isKeepAlive(httpRequest.request())) {
            httpRequest.ctx().writeAndFlush(httpResponse);
        } else {
            httpRequest.ctx().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }


    private static io.netty.handler.codec.http.HttpResponse buildHttpResponse(Object obj, HttpResponseStatus httpResponseStatus) {
        if (!(obj instanceof io.netty.handler.codec.http.HttpResponse)) {
            byte[] respBytes = obj == null ? "".getBytes() : JsonTools.DEFAULT.toJson(obj).getBytes();
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, Unpooled.wrappedBuffer(respBytes));
            fullHttpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, respBytes.length);
            fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");

            fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
            fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*");


            return fullHttpResponse;
        }
        return (io.netty.handler.codec.http.HttpResponse) obj;
    }


}
