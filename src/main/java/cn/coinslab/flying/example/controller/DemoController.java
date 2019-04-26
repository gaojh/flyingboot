package cn.coinslab.flying.example.controller;

import cn.coinslab.flying.example.bean.DemoBean;
import cn.coinslab.flying.example.service.DemoService;
import cn.coinslab.flying.ioc.annotation.Autowired;
import cn.coinslab.flying.mvc.annotation.Controller;
import cn.coinslab.flying.mvc.annotation.RequestBody;
import cn.coinslab.flying.mvc.annotation.RequestMapping;
import cn.coinslab.flying.mvc.annotation.RequestParam;

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
