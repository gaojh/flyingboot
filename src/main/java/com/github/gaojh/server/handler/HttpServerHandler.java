package com.github.gaojh.server.handler;

import com.github.gaojh.server.context.HttpContext;
import com.github.gaojh.server.http.DefaultHttpDispatcher;
import com.github.gaojh.server.http.HttpDispatcher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

/**
 * @author 高建华
 * @date 2019-03-30 22:40
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private HttpDispatcher httpDispatcher;

    public HttpServerHandler() {
        httpDispatcher = new DefaultHttpDispatcher();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }
        FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;

        if (fullHttpRequest.method().equals(HttpMethod.OPTIONS)) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*");

            ctx.writeAndFlush(response);
            ReferenceCountUtil.release(msg);
            return;
        }
        HttpContext httpContext = new HttpContext(ctx, fullHttpRequest);
        httpDispatcher.doDispatcher(httpContext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
