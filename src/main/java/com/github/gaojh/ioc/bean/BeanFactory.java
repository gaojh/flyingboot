package com.github.gaojh.ioc.bean;

import cn.hutool.core.convert.Convert;

/**
 * @author 高建华
 * @date 2019-04-28 21:13
 */
public class BeanFactory extends BeanCreator {

    public Object getBean(String name) {
        BeanDefine beanDefine = getBeanDefine(name);
        if (beanDefine != null) {
            return beanDefine.getObject();
        }
        return null;
    }

    public <T> T getBean(Class<T> clazz) {
        BeanDefine beanDefine = getBeanDefine(getBeanName(clazz));
        if (beanDefine != null) {
            return Convert.convert(clazz, beanDefine.getObject());
        }
        return null;
    }

    public void registerBean(Object object) {
        createBeanDefine(object);
    }
}
