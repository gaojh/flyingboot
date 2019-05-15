package com.github.gaojh.example.controller;

import com.github.gaojh.example.bean.DemoBean;
import com.github.gaojh.example.service.DemoService;
import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.mvc.annotation.Controller;
import com.github.gaojh.mvc.annotation.RequestBody;
import com.github.gaojh.mvc.annotation.RequestMapping;
import com.github.gaojh.mvc.annotation.RequestParam;

/**
 * @author 高建华
 * @date 2019-04-01 11:11
 */
@Controller
public class DemoController {


    @Autowired
    private DemoService demoService;

    @RequestMapping("/h")
    public String hello(@RequestParam String name){
        return demoService.getName(name);
    }

    @RequestMapping("/demo")
    public Object demo(@RequestBody DemoBean demoBean){
        return demoBean;
    }
}
