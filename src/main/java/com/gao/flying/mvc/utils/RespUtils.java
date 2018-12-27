package com.gao.flying.mvc.utils;

import com.alibaba.fastjson.JSONObject;
import com.gao.flying.mvc.http.FlyingRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;

/**
 * @author 高建华
 * @date 2018/6/24 下午10:52
 */
public class RespUtils {

    public static void sendResp(FlyingRequest flyingRequest, Object resp) {
        HttpResponse objToFlush = buildResponse(resp);
        if (HttpUtil.isKeepAlive(flyingRequest.httpRequest())) {
            flyingRequest.ctx().writeAndFlush(objToFlush);
        } else {
            flyingRequest.ctx().writeAndFlush(objToFlush).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void sendError(FlyingRequest flyingRequest, String msg, HttpResponseStatus status) {
        JSONObject json = new JSONObject(true);
        json.put("success", false);
        json.put("code", status.toString());
        json.put("msg", msg);
        HttpResponse objToFlush = buildErrorResponse(json);
        if (HttpUtil.isKeepAlive(flyingRequest.httpRequest())) {
            flyingRequest.ctx().writeAndFlush(objToFlush);
        } else {
            flyingRequest.ctx().writeAndFlush(objToFlush).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static HttpResponse buildResponse(Object obj) {

        if (!(obj instanceof HttpResponse)) {

            byte[] respBytes = obj == null ? "".getBytes() : JSONObject.toJSONString(obj).getBytes();
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(respBytes));
            fullHttpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, respBytes.length);
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            return fullHttpResponse;
        }
        return (HttpResponse) obj;
    }

    private static HttpResponse buildErrorResponse(Object obj) {

        if (!(obj instanceof HttpResponse)) {
            byte[] respBytes = JSONObject.toJSONString(obj).getBytes();
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(respBytes));
            fullHttpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, respBytes.length);
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            return fullHttpResponse;
        }
        return (HttpResponse) obj;
    }
}
