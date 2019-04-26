package com.github.flying.mvc.http;

import cn.hutool.core.io.FileUtil;
import com.github.flying.mvc.multipart.FileItem;
import com.github.flying.mvc.multipart.MimeType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

/**
 * @author 高建华
 * @date 2018/6/22 下午11:22
 */
public class FlyingHttpRequest implements HttpRequest {

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private FullHttpRequest httpRequest;
    private ChannelHandlerContext ctx;
    private String url;
    private Map<String, List<String>> parameters = new HashMap<>();
    private String body;
    private Map<String, String> headers;
    private boolean keepAlive;
    private Map<String, FileItem> fileItems = new HashMap<>();

    public FlyingHttpRequest(FullHttpRequest httpRequest, ChannelHandlerContext ctx) {
        this.httpRequest = httpRequest;
        this.ctx = ctx;
        this.keepAlive = HttpUtil.isKeepAlive(httpRequest);
        this.body = httpRequest.content().toString(Charset.forName("UTF-8"));
        init(httpRequest);
    }

    private void init(FullHttpRequest httpRequest) {
        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
        this.url = decoder.path();
        if (null == this.url || this.url.isEmpty() || "/".equals(this.url)) {
            this.url = "/index.html";
        }
        this.parameters.putAll(decoder.parameters());

        HttpHeaders httpHeaders = httpRequest.headers();
        if (httpHeaders.isEmpty()) {
            this.headers = new HashMap<>();
        } else {
            this.headers = new HashMap<>(httpHeaders.size());
            Iterator<Map.Entry<String, String>> entryIterator = httpHeaders.iteratorAsString();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> entry = entryIterator.next();
                headers.put(entry.getKey(), entry.getValue());
            }
        }

        if (HttpMethod.POST.equals(httpRequest.method()) && isFormRequest()) {
            HttpPostRequestDecoder httpPostRequestDecoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, httpRequest);
            httpPostRequestDecoder.getBodyHttpDatas().forEach(this::parseData);
        }

    }

    private void parseData(InterfaceHttpData data) {
        try {
            switch (data.getHttpDataType()) {
                case Attribute:
                    Attribute attribute = (Attribute) data;
                    String name = attribute.getName();
                    String value = attribute.getValue();

                    List<String> values;
                    if (this.parameters.containsKey(name)) {
                        values = this.parameters.get(name);
                        values.add(value);
                    } else {
                        values = new ArrayList<>();
                        values.add(value);
                        this.parameters.put(name, values);
                    }

                    break;
                case FileUpload:
                    FileUpload fileUpload = (FileUpload) data;
                    parseFileUpload(fileUpload);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            data.release();
        }
    }

    private void parseFileUpload(FileUpload fileUpload) throws IOException {
        if (fileUpload.isCompleted()) {
            String ext = FileUtil.extName(fileUpload.getFilename());
            String contentType = MimeType.get(ext);
            if (null == contentType) {
                contentType = URLConnection.guessContentTypeFromName(fileUpload.getFilename());
            }
            if (fileUpload.isInMemory()) {
                FileItem fileItem = new FileItem(fileUpload.getName(), fileUpload.getFilename(),
                        contentType, fileUpload.length());

                ByteBuf byteBuf = fileUpload.getByteBuf();
                fileItem.setData(ByteBufUtil.getBytes(byteBuf));
                fileItems.put(fileItem.getName(), fileItem);
            } else {
                FileItem fileItem = new FileItem(fileUpload.getName(), fileUpload.getFilename(),
                        contentType, fileUpload.length());
                byte[] bytes = Files.readAllBytes(fileUpload.getFile().toPath());
                fileItem.setData(bytes);
                fileItems.put(fileItem.getName(), fileItem);
            }
        }
    }


    @Override
    public String host() {
        return header("Host");
    }

    @Override
    public String uri() {
        return this.httpRequest.uri();
    }

    @Override
    public String url() {
        return this.url;
    }

    @Override
    public Map<String, List<String>> parameters() {
        return parameters;
    }

    @Override
    public Map<String, String> headers() {
        return this.headers;
    }

    @Override
    public Map<String, FileItem> fileItems() {
        return fileItems;
    }

    @Override
    public String body() {
        return this.body;
    }

    @Override
    public boolean keepAlive() {
        return this.keepAlive;
    }

    @Override
    public ChannelHandlerContext ctx() {
        return this.ctx;
    }

    @Override
    public HttpMethod method() {
        return this.httpRequest.method();
    }

    @Override
    public FullHttpRequest request() {
        return this.httpRequest;
    }
}
