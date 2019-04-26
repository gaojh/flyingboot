package com.github.flying.ioc.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 高建华
 * @date 2018/7/7 下午9:21
 */
@Data
@NoArgsConstructor
public class BeanDefine {

    private Object bean;
    private Class<?> type;

    public BeanDefine(Object bean) {
        this(bean, bean.getClass());
    }

    public BeanDefine(Object bean, Class<?> type) {
        this.bean = bean;
        this.type = type;
    }

}
