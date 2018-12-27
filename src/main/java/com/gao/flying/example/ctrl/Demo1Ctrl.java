package com.gao.flying.example.ctrl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.gao.flying.example.service.Service1;
import com.gao.flying.example.service.Service2;
import com.gao.flying.ioc.annotation.Inject;
import com.gao.flying.ioc.annotation.Value;
import com.gao.flying.mvc.annotation.*;
import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.utils.PathMatcher;

/**
 * @author 高建华
 * @date 2018/7/7 下午10:59
 */
@Ctrl
public class Demo1Ctrl {

    @Inject
    private Service1 service1;

    @Inject
    private Service2 service2;

    @Value("${demo.port}")
    private int port;

    @Value("${demo.pass}")
    private String pass;

    @Route("/hello")
    public String hello(@RequestParam String name) {
        return service1.m1(name) + "-" + pass + ":" + port;
    }

    @Route(value = "/user")
    public String user(@RequestBody User user) {
        return JSONUtil.toJsonStr(user);
    }

    @Route("/{url}")
    public String test1(@PathParam String url, FlyingRequest flyingRequest) {
        System.out.println(url);
        return url;
    }

    public static void main(String[] args) {
    }
}