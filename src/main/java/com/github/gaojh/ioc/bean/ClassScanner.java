package com.github.gaojh.ioc.bean;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * @author gaojianhua
 * @date 2019/12/10 1:33 下午
 */
public interface ClassScanner {

    /**
     * 根据名称获取Class定义
     * @param name 名称
     * @return
     */
    ClassDefine getClassDefine(String name);

    /**
     * 获取所有的class define
     * @return
     */
    Collection<ClassDefine> getClassDefines();

    /**
     * 根据接口获取classDefine
     * @param clazz
     * @return
     */
    List<ClassDefine> getClassDefineByInterface(Class<?> clazz);

    /**
     * 根据注解获取classDefine
     * @param clazz
     * @return
     */
    List<Class<?>> getClassDefineByAnnotation(Class<? extends Annotation> clazz);

    /**
     * 获取bean名称
     * @param clazz
     * @return
     */
    String getBeanName(Class<?> clazz);
}
