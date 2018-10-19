package com.gao.flying.netty.handler;

import cn.hutool.core.thread.ThreadUtil;
import com.gao.flying.context.ServerContext;
import com.gao.flying.mvc.http.*;
import com.gao.flying.mvc.utils.RespUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author 高建华
 * @date 2018/5/4 下午11:56
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private ServerContext serverContext;

    public HttpServerHandler(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }

        Request request = new DefaultRequest((FullHttpRequest) msg, ctx);
        Response response = DefaultResponse.buildSuccess();
        try {
            Dispatcher.me.doDispathcer(serverContext, request, response);
        } catch (Exception e) {
            RespUtils.sendError(request, e.getMessage(), HttpResponseStatus.BAD_REQUEST);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
