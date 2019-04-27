package com.github.gaojh.example.service;

import com.github.gaojh.ioc.annotation.Component;

/**
 * @author 高建华
 * @date 2019-04-01 11:06
 */
@Component
public class DemoService {

    public String getName(String name) {
        return "new name is " + name;
    }
}
