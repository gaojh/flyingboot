package com.github.gaojh.ioc.bean;

import lombok.Data;

/**
 * @author 高建华
 * @date 2018/7/7 下午9:21
 */
@Data
public class BeanDefine {

    /**
     * bean名称
     */
    private String name;

    /**
     * bean class
     */
    private Class<?> type;

    /**
     * bean instance
     */
    private Object object;

    /**
     * 是否是单例，默认都是单例，此参数未使用
     */
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
