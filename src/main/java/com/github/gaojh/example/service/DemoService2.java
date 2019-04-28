package com.github.gaojh.example.service;

import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.ioc.annotation.Component;

/**
 * @author 高建华
 * @date 2019-04-27 19:36
 */
@Component
public class DemoService2 {

    @Autowired
    private DemoService3 demoService3;
}
