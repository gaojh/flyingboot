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
public class BeanClassDefine {
    private String beanName;
    private Class<?> beanClass;
    private boolean isConfigurationBean;
    private Class<?> configurationClass;
    private Method method;
}
