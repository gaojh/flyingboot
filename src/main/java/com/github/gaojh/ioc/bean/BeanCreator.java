package com.github.gaojh.ioc.bean;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author gaojianhua
 * @date 2019/12/10 1:38 下午
 */
public interface BeanCreator {

    /**
     * 根据名称获取bean
     * @param name
     * @return
     */
    BeanDefine getBeanDefine(String name);

    /**
     * 根据class 获取bean
     * @param clazz
     * @return
     */
    BeanDefine getBeanDefine(Class<?> clazz);

    /**
     * 根据实例创建bean
     * @param object
     * @return
     */
    BeanDefine createBeanDefine(Object object);

    /**
     * 根据名称和实例创建bean
     * @param name
     * @param object
     * @return
     */
    BeanDefine createBeanDefine(String name, Object object);

    /**
     * 根据注解获取bean
     * @param annotationClass
     * @return
     */
    List<BeanDefine> getBeanDefineByAnnotation(Class<? extends Annotation> annotationClass);

}
