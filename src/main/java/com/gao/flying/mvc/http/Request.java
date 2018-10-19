package com.gao.flying.mvc.http;

import com.gao.flying.mvc.multipart.FileItem;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * @author 高建华
 * @date 2018/7/5 上午11:18
 */
public interface Request {

    String host();

    String uri();

    String url();

    Map<String,List<String>> parameters();

    Map<String, String> headers();

    Map<String, FileItem> fileItems();

    default String header(@NonNull String name) {
        String header = "";
        if (headers().containsKey(name)) {
            header = headers().get(name);
        } else if (headers().containsKey(name.toLowerCase())) {
            header = headers().get(name.toLowerCase());
        }
        return header;
    }

    default boolean isFormRequest() {
        return this.header("Content-Type").toLowerCase().contains("form");
    }

    default boolean isJsonRequest() {
        return this.header("Content-Type").toLowerCase().contains("json");
    }

    String body();

    boolean keepAlive();

    ChannelHandlerContext ctx();

    HttpMethod method();

    FullHttpRequest httpRequest();

}
