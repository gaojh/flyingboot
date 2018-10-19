package com.gao.flying.example.service;

import com.gao.flying.ioc.annotation.Bean;

/**
 * @author 高建华
 * @date 2018/7/8 下午11:39
 */
@Bean
public class Service4Impl implements Service4 {
    @Override
    public String hello(String name) {
        return "service-4-"+name;
    }
}
