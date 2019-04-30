package com.github.gaojh.example.service;

import com.github.gaojh.config.Environment;
import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.ioc.annotation.Component;

/**
 * @author 高建华
 * @date 2019-04-01 11:06
 */
@Component
public class DemoService {

    @Autowired
    public DemoService(Environment environment){

    }

    public String getName(String name) {
        return "new name is " + name;
    }
}
