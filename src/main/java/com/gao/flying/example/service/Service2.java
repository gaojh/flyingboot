package com.gao.flying.example.service;

import com.gao.flying.ioc.annotation.Bean;
import com.gao.flying.ioc.annotation.Inject;

/**
 * @author 高建华
 * @date 2018/7/7 下午11:00
 */
@Bean
public class Service2 {

    @Inject
    private Service1 service1;

    public String m1(String name) {
        return "service-2-m1-" + name;
    }
}
