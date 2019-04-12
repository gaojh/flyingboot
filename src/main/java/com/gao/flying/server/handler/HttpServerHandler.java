package com.gao.flying.server.handler;

import com.gao.flying.context.FlyingContext;
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

    private FlyingContext flyingContext;

    public HttpServerHandler(FlyingContext flyingContext) {
        this.flyingContext = flyingContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }

        HttpRequest httpRequest = new FlyingHttpRequest((FullHttpRequest) msg, ctx);
        HttpResponse httpResponse = FlyingHttpResponse.buildSuccess(ctx);
        HttpRoute httpRoute = flyingContext.fetchGetRoute(httpRequest.url());

        if (httpRoute == null) {
            httpResponse.success(false).msg("未找到对应的处理器").httpResponseStatus(HttpResponseStatus.BAD_REQUEST);
            RespUtils.sendResponse(httpRequest, httpResponse);
            return;
        }

        try {
            Dispatcher.me.execute(flyingContext, httpRequest, httpResponse);
        } catch (Exception e) {
            httpResponse.success(false).msg("服务器内部调用异常：" + e.getMessage()).httpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            RespUtils.sendResponse(httpRequest, httpResponse);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
