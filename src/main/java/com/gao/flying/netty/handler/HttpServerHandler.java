package com.gao.flying.netty.handler;

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

        FlyingRequest flyingRequest = new DefaultFlyingRequest((FullHttpRequest) msg, ctx);
        FlyingResponse flyingResponse = DefaultFlyingResponse.buildSuccess();

        if(serverContext.executeFilter(flyingRequest,flyingResponse)) {
            try {
                Dispatcher.me.doDispathcer(serverContext, flyingRequest, flyingResponse);
            } catch (Exception e) {
                RespUtils.sendError(flyingRequest, e.getMessage(), HttpResponseStatus.BAD_REQUEST);
            }
        }else{
            RespUtils.sendResp(flyingRequest,flyingResponse.data());
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
