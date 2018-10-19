package com.gao.flying.example.service;

import com.gao.flying.ioc.annotation.Bean;

/**
 * @author 高建华
 * @date 2018/7/8 下午10:12
 */
@Bean
public class Service3 {

    public String hh(String name){
        return "service-3-"+name;
    }
}
