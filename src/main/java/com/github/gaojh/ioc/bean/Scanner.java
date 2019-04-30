package com.github.gaojh.ioc.bean;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.ClassUtil;
import com.github.gaojh.config.ApplicationConfig;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * @author 高建华
 * @date 2019-04-28 15:23
 */
public interface Scanner {

    /**
     * 扫描注解类
     *
     * @param classes
     * @return
     */
    default Set<Class<?>> scan(List<Class<? extends Annotation>> classes) {
        if (classes == null || classes.size() == 0) {
            return new ConcurrentHashSet<>();
        }
        Set<Class<?>> beans = new ConcurrentHashSet<>();
        for (String basePkg : ApplicationConfig.BASE_PACKAGE) {
            for (Class<? extends Annotation> clazz : classes) {
                beans.addAll(ClassUtil.scanPackageByAnnotation(basePkg, clazz));
            }
        }
        return beans;
    }

}
