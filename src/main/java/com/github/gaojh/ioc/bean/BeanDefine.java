package com.github.gaojh.ioc.bean;

import lombok.Data;

/**
 * @author 高建华
 * @date 2018/7/7 下午9:21
 */
@Data
public class BeanDefine {

    private String name;
    private Class<?> type;
    private Object object;
    private boolean isSingleton;

    public BeanDefine(Object object) {
        this(object.getClass(), object);
    }

    public BeanDefine(Class<?> type, Object object) {
        this(type.getName(), type, object);
    }

    public BeanDefine(String name, Class<?> type, Object object) {
        this.name = name;
        this.object = object;
        this.type = type;
    }

}
