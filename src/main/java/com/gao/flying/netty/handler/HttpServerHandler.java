package com.gao.flying.netty.handler;

import com.gao.flying.context.ServerContext;
import com.gao.flying.mvc.http.*;
import com.gao.flying.mvc.utils.RespUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

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
        FlyingResponse flyingResponse = DefaultFlyingResponse.buildSuccess(ctx);

        try {
            Dispatcher.me.execute(serverContext, flyingRequest, flyingResponse);
        } catch (Exception e) {
            flyingResponse.success(false).msg("服务器内部调用异常：" + e.getMessage()).httpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            RespUtils.sendResponse(flyingResponse);
        } finally {
            ReferenceCountUtil.release(flyingRequest.httpRequest());
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
