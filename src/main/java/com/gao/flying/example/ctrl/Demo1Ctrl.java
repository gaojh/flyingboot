package com.gao.flying.example.ctrl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.gao.flying.example.service.Service1;
import com.gao.flying.example.service.Service2;
import com.gao.flying.ioc.annotation.Inject;
import com.gao.flying.ioc.annotation.Value;
import com.gao.flying.mvc.annotation.*;
import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.utils.PathMatcher;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

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
    public String user(@RequestBody List<User> users) {
        return JSONUtil.toJsonStr(users);
    }

    @Route(value = "/user2")
    public String user2(@RequestBody List users) {
        return JSONUtil.toJsonStr(users);
    }

    @Route(value = "/user3")
    public String user3(@RequestBody CommonUser<User> commonUser) {
        return JSONUtil.toJsonStr(commonUser);
    }

    @Route("/{url}")
    public String test1(@PathParam String url, FlyingRequest flyingRequest) {
        System.out.println(url);
        return url;
    }

    public static void main(String[] args) {
        List<User> list = new ArrayList<>();
        User user = new User();
    }
}