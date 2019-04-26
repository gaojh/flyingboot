package com.github.flyingboot.example.controller;

import com.github.flyingboot.example.bean.DemoBean;
import com.github.flyingboot.example.service.DemoService;
import com.github.flyingboot.ioc.annotation.Autowired;
import com.github.flyingboot.mvc.annotation.Controller;
import com.github.flyingboot.mvc.annotation.RequestBody;
import com.github.flyingboot.mvc.annotation.RequestMapping;
import com.github.flyingboot.mvc.annotation.RequestParam;

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
