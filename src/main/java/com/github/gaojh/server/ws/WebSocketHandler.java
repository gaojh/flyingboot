package com.github.gaojh.server.ws;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author 高建华
 * @date 2019-07-25 14:14
 * @description
 */
public interface WebSocketHandler {

    void onOpen(ChannelHandlerContext ctx) throws Exception;

    void onMessage(ChannelHandlerContext ctx, String msg) throws Exception;

    void onClose(ChannelHandlerContext ctx) throws Exception;

    /**
     * websocket消息发送方式
     *
     * @param ctx ctx
     * @param msg 消息
     * @return ChannelFuture
     */
    default ChannelFuture sendMessage(ChannelHandlerContext ctx, String msg) {
        return ctx.writeAndFlush(new TextWebSocketFrame(msg));
    }
}
