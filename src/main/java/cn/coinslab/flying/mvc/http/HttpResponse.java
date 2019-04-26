package cn.coinslab.flying.mvc.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author 高建华
 * @date 2018/7/5 上午11:18
 */
public interface HttpResponse {

    ChannelHandlerContext ctx();

    HttpResponse success(boolean res);

    boolean success();

    HttpResponse msg(String msg);

    String msg();

    HttpResponse data(Object data);

    Object data();

    HttpResponse httpResponseStatus(HttpResponseStatus httpResponseStatus);

    HttpResponseStatus httpResponseStatus();
}
