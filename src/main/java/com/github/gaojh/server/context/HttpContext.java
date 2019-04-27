package com.github.gaojh.server.context;

import com.github.gaojh.mvc.http.FlyingHttpRequest;
import com.github.gaojh.mvc.http.FlyingHttpResponse;
import com.github.gaojh.mvc.Mvcs;
import com.github.gaojh.mvc.http.HttpRequest;
import com.github.gaojh.mvc.http.HttpResponse;
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
        this.httpResponse = FlyingHttpResponse.buildSuccess(ctx);
        Mvcs.request.set(this.httpRequest);
        Mvcs.response.set(this.httpResponse);
    }
}
