package com.github.gaojh.example.handler;

import cn.hutool.core.util.StrUtil;
import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.server.http.HttpRequest;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 高建华
 * @date 2018-12-14 15:08
 */
@Component
public class HttpClient {

    private static final MediaType MEDIA_TYPE = MediaType.parse("Content-Type: application/json;");
    private OkHttpClient client;

    public HttpClient() {
        init();
    }

    private void init() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        httpBuilder.connectTimeout(60, TimeUnit.SECONDS);
        httpBuilder.connectionPool(new ConnectionPool(2000, 10, TimeUnit.MINUTES));
        client = httpBuilder.build();
    }

    public HttpResponse request(String url, HttpRequest httpRequest) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        for (Map.Entry<String, String> headerEntry : httpRequest.request().headers()) {
            if (StrUtil.isNotEmpty(headerEntry.getKey())
                    && !"Host".equals(headerEntry.getKey())) {
                builder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        builder.addHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());

        if (httpRequest.method().equals(HttpMethod.POST)) {
            builder.post(RequestBody.create(MEDIA_TYPE, httpRequest.body()));
        } else {
            builder.get();
        }

        Response response;
        FullHttpResponse fullHttpResponse;
        try {
            response = client.newCall(builder.build()).execute();
            ResponseBody responseBody = response.body();
            assert responseBody != null;
            byte[] bytes = responseBody.bytes();
            fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));
            Headers headers = response.headers();
            for(String key : headers.names()){
                fullHttpResponse.headers().add(key,response.header(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
            fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        }


        return fullHttpResponse;
    }
}
