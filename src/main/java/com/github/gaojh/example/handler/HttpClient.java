package com.github.gaojh.example.handler;

import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.server.http.HttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author 高建华
 * @date 2018-12-14 15:08
 */
@Component
public class HttpClient {


    private static final MediaType MEDIA_TYPE = MediaType.parse("Content-Type: application/json;");
    private OkHttpClient client;

    public HttpClient(){
        init();
    }

    private void init() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        httpBuilder.connectTimeout(30, TimeUnit.SECONDS);
        httpBuilder.connectionPool(new ConnectionPool(2000, 10, TimeUnit.MINUTES));
        client = httpBuilder.build();
    }

    public Request createRequest(String url, HttpRequest flyingRequest) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        flyingRequest.headers().forEach(builder::addHeader);
        builder.addHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
        if (flyingRequest.method().equals(HttpMethod.POST)) {
            builder.post(createRequestBody(flyingRequest));
        } else {
            builder.get();
        }
        return builder.build();
    }

    public RequestBody createRequestBody(HttpRequest flyingRequest) {
        return RequestBody.create(MEDIA_TYPE, flyingRequest.body());
    }

    public Object request(Request request) {
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return responseBody.string().replaceAll("\\\"","");
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
