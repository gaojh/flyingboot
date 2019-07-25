package com.github.gaojh.example.handler;

import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.mvc.route.RouterHandler;
import com.github.gaojh.server.http.HttpRequest;


/**
 * @author 高建华
 * @date 2019-04-30 10:00
 */
@Component
public class DemoDynamicHandler2 implements RouterHandler {

    @Autowired
    private HttpClient httpClient;

    @Override
    public Object handle(HttpRequest httpRequest) {
        return httpClient.request("http://localhost:12345", httpRequest);
    }
}
