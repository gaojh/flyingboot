package com.github.gaojh.ioc.bean;

import cn.hutool.core.convert.Convert;
import com.github.gaojh.config.Environment;

/**
 * @author 高建华
 * @date 2019-04-28 21:13
 */
public class BeanContext extends BeanDefineCreator {

    public BeanContext(Environment environment){
        super(environment);
    }

    public Object getBean(String name) {
        BeanDefine beanDefine = this.getBeanDefine(name);
        if (beanDefine != null) {
            return beanDefine.getObject();
        }
        return null;
    }

    public <T> T getBean(Class<T> clazz) {
        BeanDefine beanDefine = this.getBeanDefine(clazz);
        if (beanDefine != null) {
            return Convert.convert(clazz, beanDefine.getObject());
        }
        return null;
    }

    public void registerBean(Object object) {
        this.createBeanDefine(object);
    }

}
