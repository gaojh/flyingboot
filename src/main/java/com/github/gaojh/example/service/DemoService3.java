package com.github.gaojh.example.service;

import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.ioc.annotation.Component;

/**
 * @author 高建华
 * @date 2019-04-27 19:37
 */
@Component
public class DemoService3 {

    @Autowired
    private DemoService1 demoService1;
}
