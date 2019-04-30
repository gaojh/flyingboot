package com.github.gaojh.example.handler;

import cn.hutool.http.HttpUtil;
import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.mvc.route.DynamicRouteHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author 高建华
 * @date 2019-04-30 10:00
 */
@Component
public class DemoDynamicHandler implements DynamicRouteHandler {

    @Override
    public Object handle(FullHttpRequest fullHttpRequest) {
        return HttpUtil.get("http://www.baidu.com");
    }
}
