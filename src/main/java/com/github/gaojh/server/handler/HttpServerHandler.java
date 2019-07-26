package com.github.gaojh.server.handler;

import com.github.gaojh.config.ApplicationConfig;
import com.github.gaojh.ioc.context.ApplicationUtil;
import com.github.gaojh.server.context.HttpContext;
import com.github.gaojh.server.http.DefaultHttpDispatcher;
import com.github.gaojh.server.http.HttpDispatcher;
import com.github.gaojh.server.ws.WebSocketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;

/**
 * @author 高建华
 * @date 2019-03-30 22:40
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private HttpDispatcher httpDispatcher;
    private WebSocketHandler webSocketHandler;
    private WebSocketServerHandshaker handshaker;

    public HttpServerHandler() {
        httpDispatcher = new DefaultHttpDispatcher();
        webSocketHandler = ApplicationUtil.applicationContext.getBean(WebSocketHandler.class);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (webSocketHandler != null) {
            webSocketHandler.onClose(ctx);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            handleHttpRequest(ctx, fullHttpRequest);
        } else if (msg instanceof WebSocketFrame) {
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        if (!fullHttpRequest.decoderResult().isSuccess() || (!"websocket".equals(fullHttpRequest.headers().get("Upgrade")))) {
            if (fullHttpRequest.method().equals(HttpMethod.OPTIONS)) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*");

                ctx.writeAndFlush(response);
                ReferenceCountUtil.release(fullHttpRequest);
                return;
            }
            HttpContext httpContext = new HttpContext(ctx, fullHttpRequest);
            httpDispatcher.doDispatcher(httpContext);
        } else {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:" + ApplicationConfig.PORT, null, false);
            handshaker = wsFactory.newHandshaker(fullHttpRequest);
            if (handshaker == null || !ApplicationConfig.ENABLE_WEBSOCKET) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), fullHttpRequest);
                webSocketHandler.onHandshake(ctx, fullHttpRequest);
            }
        }
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
        // 处理消息
        String request = ((TextWebSocketFrame) frame).text();
        webSocketHandler.onMessage(ctx, request);
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
