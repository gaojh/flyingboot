package com.github.gaojh.ioc.bean;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author 高建华
 * @date 2019-05-23 10:25
 */
@Data
@Builder
public class ClassDefine {

    /**
     * bean名称
     */
    private String beanName;

    /**
     * bean类
     * 1、如果是非@Configuration注解的类，则是其本身
     * 2、如果是@Configuration注解的类型，则是@Bean的returnType的类
     */
    private Class<?> beanClass;

    /**
     * 是否是@Configuration注解的
     */
    private boolean isConfigurationBean;

    /**
     * 使用@Configuration注解的类，如果不是，则是空
     */
    private Class<?> configurationClass;

    /**
     * 使用在@Configuration中使用@Bean注解的方法
     */
    private Method method;


}
