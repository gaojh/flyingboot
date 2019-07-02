package com.github.gaojh.example.config;

import com.github.gaojh.example.handler.DemoDynamicHandler;
import com.github.gaojh.example.handler.DemoDynamicHandler2;
import com.github.gaojh.example.handler.HttpClient;
import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.ioc.annotation.Bean;
import com.github.gaojh.ioc.annotation.Configuration;
import com.github.gaojh.mvc.route.RouterFunction;
import com.github.gaojh.mvc.route.Routers;

/**
 * @author 高建华
 * @date 2019-05-10 14:37
 */
@Configuration
public class RouterConfig {

    @Autowired
    private DemoDynamicHandler demoDynamicHandler;
    @Autowired
    private DemoDynamicHandler2 demoDynamicHandler2;

    @Autowired
    private HttpClient httpClient;

    @Bean
    public RouterFunction router() {
        return Routers.route().GET("/api/baidu", httpRequest -> httpClient.request("http://www.taobao.com", httpRequest)).GET("/hello", demoDynamicHandler).GET("/hello2", demoDynamicHandler2).build();
    }

}
