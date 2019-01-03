package com.gao.flying.mvc.utils;

import com.alibaba.fastjson.JSONObject;
import com.gao.flying.mvc.http.FlyingResponse;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

/**
 * @author 高建华
 * @date 2018/6/24 下午10:52
 */
public class RespUtils {

    public static void sendResponse(FlyingResponse flyingResponse) {
        if (flyingResponse.success()) {
            HttpResponse httpResponse = buildHttpResponse(flyingResponse.data(),HttpResponseStatus.OK);
            flyingResponse.ctx().writeAndFlush(httpResponse);
        } else {
            HttpResponse httpResponse = buildHttpResponse(flyingResponse.msg(),flyingResponse.httpResponseStatus());
            flyingResponse.ctx().writeAndFlush(httpResponse);
        }
    }


    private static HttpResponse buildHttpResponse(Object obj, HttpResponseStatus httpResponseStatus) {
        if (!(obj instanceof HttpResponse)) {
            byte[] respBytes = obj == null ? "".getBytes() : JSONObject.toJSONString(obj).getBytes();
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, Unpooled.wrappedBuffer(respBytes));
            fullHttpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, respBytes.length);
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            return fullHttpResponse;
        }
        return (HttpResponse) obj;
    }


}
