package com.gao.flying.example.controller;

import com.gao.flying.example.bean.DemoBean;
import com.gao.flying.example.service.DemoService;
import com.gao.flying.ioc.annotation.Autowired;
import com.gao.flying.mvc.annotation.Controller;
import com.gao.flying.mvc.annotation.RequestBody;
import com.gao.flying.mvc.annotation.RequestMapping;
import com.gao.flying.mvc.annotation.RequestParam;

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
