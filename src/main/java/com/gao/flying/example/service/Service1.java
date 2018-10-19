package com.gao.flying.example.service;

import com.gao.flying.ioc.annotation.Bean;
import com.gao.flying.ioc.annotation.Inject;

/**
 * @author 高建华
 * @date 2018/7/7 下午11:00
 */
@Bean
public class Service1 {

    @Inject
    private Service2 service2;

    @Inject
    private Service4 service4;

    public String m1(String name) {
        System.out.println(service2.m1(name));
        return service4.hello(name);
    }
}
