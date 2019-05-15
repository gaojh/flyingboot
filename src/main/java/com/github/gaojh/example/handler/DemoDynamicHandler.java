package com.github.gaojh.example.handler;

import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.server.http.HttpRequest;
import com.github.gaojh.mvc.route.RouterHandler;
import okhttp3.Request;

/**
 * @author 高建华
 * @date 2019-04-30 10:00
 */
@Component
public class DemoDynamicHandler implements RouterHandler {

    @Autowired
    private HttpClient httpClient;

    @Override
    public Object handle(HttpRequest httpRequest) {
        Request request = httpClient.createRequest("http://localhost:2019/h?name=123", httpRequest);
        return httpClient.request(request);
    }
}
