package com.github.gaojh.example;

import com.github.gaojh.Flying;
import com.github.gaojh.ioc.annotation.ComponentScan;

/**
 * @author 高建华
 * @date 2019-03-31 22:43
 */
@ComponentScan({"com.github.gaojh.example"})
public class FlyingbootDemo {
    public static void main(String[] args) {
        Flying.run(FlyingbootDemo.class);
    }
}
