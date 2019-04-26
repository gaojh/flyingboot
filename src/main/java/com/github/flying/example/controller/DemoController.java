package com.github.flying.example.controller;

import com.github.flying.example.bean.DemoBean;
import com.github.flying.example.service.DemoService;
import com.github.flying.ioc.annotation.Autowired;
import com.github.flying.mvc.annotation.Controller;
import com.github.flying.mvc.annotation.RequestBody;
import com.github.flying.mvc.annotation.RequestMapping;
import com.github.flying.mvc.annotation.RequestParam;

/**
 * @author 高建华
 * @date 2019-04-01 11:11
 */
@Controller
public class DemoController {


    @Autowired
    private DemoService demoService;

    @RequestMapping
    public String hello(@RequestParam String name){
        return demoService.getName(name);
    }

    @RequestMapping("/demo")
    public Object demo(@RequestBody DemoBean demoBean){
        return demoBean;
    }
}
