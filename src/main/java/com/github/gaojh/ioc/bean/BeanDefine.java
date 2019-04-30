package com.github.gaojh.ioc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 高建华
 * @date 2018/7/7 下午9:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeanDefine {

    private Class<?> type;
    private Object object;

    public BeanDefine(Object object) {
        this(object, object.getClass());
    }

    public BeanDefine(Object object, Class<?> type) {
        this.object = object;
        this.type = type;
    }

}
