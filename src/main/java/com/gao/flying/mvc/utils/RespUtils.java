package com.gao.flying.mvc.utils;

import com.gao.flying.context.FlyingContext;
import com.gao.flying.mvc.http.HttpRequest;
import com.gao.flying.mvc.http.HttpResponse;
import com.gao.flying.server.context.HttpContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;

/**
 * @author 高建华
 * @date 2018/6/24 下午10:52
 */
public class RespUtils {

    public static void sendResponse(HttpContext httpContext){
        HttpResponse response = httpContext.getHttpResponse();
        if (HttpUtil.isKeepAlive(httpContext.getHttpRequest().request())) {
            httpContext.getCtx().writeAndFlush(buildHttpResponse(response.data(), HttpResponseStatus.OK));
        } else {
            httpContext.getCtx().writeAndFlush(buildHttpResponse(response.data(), HttpResponseStatus.OK)).addListener(ChannelFutureListener.CLOSE);
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
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, respBytes.length);
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            return fullHttpResponse;
        }
        return (io.netty.handler.codec.http.HttpResponse) obj;
    }


}
