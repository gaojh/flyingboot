package com.github.flying.example.bean;

import lombok.Data;

import java.util.List;

/**
 * @author 高建华
 * @date 2019-04-16 10:26
 */
@Data
public class DemoBean {
    private Long id;
    private String name;
    private List<Object> list;
}
