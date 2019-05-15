package com.github.gaojh.example.config;

import com.github.gaojh.example.handler.DemoDynamicHandler;
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

    @Bean
    public RouterFunction router(DemoDynamicHandler demoDynamicHandler){
       return Routers.route().GET("/hello",demoDynamicHandler).build();
    }
}
