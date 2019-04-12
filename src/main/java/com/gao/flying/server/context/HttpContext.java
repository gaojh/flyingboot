package com.gao.flying.server.context;

import com.gao.flying.mvc.http.FlyingHttpRequest;
import com.gao.flying.mvc.http.FlyingHttpResponse;
import com.gao.flying.mvc.http.HttpRequest;
import com.gao.flying.mvc.http.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 高建华
 * @date 2019-03-30 15:43
 */
public class HttpContext {

    @Getter
    private HttpRequest httpRequest;

    @Getter
    @Setter
    private HttpResponse httpResponse;

    @Getter
    private ChannelHandlerContext ctx;

    @Getter
    private FullHttpRequest request;

    @Getter
    @Setter
    private FullHttpResponse response;

    public HttpContext(ChannelHandlerContext ctx, FullHttpRequest request) {
        this.ctx = ctx;
        this.request = request;
        this.httpRequest = new FlyingHttpRequest(request, ctx);
        this.httpResponse = new FlyingHttpResponse();
    }
}
