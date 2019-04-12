package com.gao.flying.server.handler;

import com.gao.flying.mvc.http.FlyingHttpDispatcher;
import com.gao.flying.mvc.http.HttpDispatcher;
import com.gao.flying.server.context.HttpContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author 高建华
 * @date 2019-03-30 22:40
 */
public class HttpServerHandler2 extends ChannelInboundHandlerAdapter {

    private HttpDispatcher httpDispatcher;

    public HttpServerHandler2() {
        httpDispatcher = new FlyingHttpDispatcher();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }

        HttpContext httpContext = new HttpContext(ctx, (FullHttpRequest) msg);
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
