package com.github.gaojh.example.handler;

import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.server.ws.WebSocketHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author 高建华
 * @date 2019-07-25 14:47
 * @description
 */
@Component
public class MyWebSocketHandler implements WebSocketHandler {
    @Override
    public void onOpen(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void onMessage(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("msg: "+msg);
        sendMessage(ctx,"回复："+msg);
    }

    @Override
    public void onClose(ChannelHandlerContext ctx) throws Exception {

    }

}
