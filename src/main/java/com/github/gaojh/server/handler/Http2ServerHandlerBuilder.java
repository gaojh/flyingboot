package com.github.gaojh.server.handler;

import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Settings;

/**
 * @author 高建华
 * @date 2019-05-22 17:55
 */
public class Http2ServerHandlerBuilder extends AbstractHttp2ConnectionHandlerBuilder<Http2ServerHandler, Http2ServerHandlerBuilder> {
    @Override
    protected Http2ServerHandler build(Http2ConnectionDecoder http2ConnectionDecoder, Http2ConnectionEncoder http2ConnectionEncoder, Http2Settings http2Settings) throws Exception {
        Http2ServerHandler http2ServerHandler = new Http2ServerHandler(http2ConnectionDecoder, http2ConnectionEncoder, http2Settings);
        frameListener(http2ServerHandler);
        return http2ServerHandler;
    }
}
