package com.github.gaojh.example.handler;

import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.mvc.route.RouterHandler;
import com.github.gaojh.server.http.HttpRequest;


/**
 * @author 高建华
 * @date 2019-04-30 10:00
 */
@Component
public class DemoDynamicHandler implements RouterHandler {


    @Override
    public Object handle(HttpRequest httpRequest) {
        return "ok";
    }
}
