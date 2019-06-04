package com.github.gaojh.ioc.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.gaojh.ioc.annotation.Bean;
import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.ioc.annotation.Configuration;
import com.github.gaojh.mvc.annotation.Controller;
import com.github.gaojh.mvc.annotation.Interceptor;
import com.github.gaojh.mvc.annotation.Setup;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 高建华
 * @date 2019-04-28 21:25
 */
public class BeanClassScanner implements Scanner {

    private ConcurrentHashMap<String, BeanClassDefine> beanDefineClassMap = new ConcurrentHashMap<>();

    public BeanClassScanner() {
        List<Class<? extends Annotation>> annotationList = CollUtil.toList(Configuration.class, Component.class, Controller.class, Setup.class, Interceptor.class);

        scan(annotationList).forEach(clazz -> {
            String beanName = getBeanName(clazz);
            beanDefineClassMap.put(beanName, BeanClassDefine.builder().beanClass(clazz).beanName(beanName).isConfigurationBean(false).build());
            if (clazz.isAnnotationPresent(Configuration.class)) {
                Arrays.stream(clazz.getMethods()).filter(method -> method.isAnnotationPresent(Bean.class)).forEach(method -> {
                    String methodBeanName = getBeanName(method.getReturnType());
                    BeanClassDefine beanClassDefine = BeanClassDefine.builder().beanName(methodBeanName).beanClass(method.getReturnType()).isConfigurationBean(true).configurationClass(clazz).method(method).build();
                    beanDefineClassMap.put(methodBeanName, beanClassDefine);
                });
            }
        });

    }

    protected BeanClassDefine getBeanClassDefine(String name) {
        return beanDefineClassMap.get(name);
    }

    private List<BeanClassDefine> getBeanClassOfType(Class<?> clazz) {
        return beanDefineClassMap.values().stream().filter(beanClassDefine -> beanClassDefine.getBeanClass().isAssignableFrom(clazz)).collect(Collectors.toList());
    }

    protected Collection<BeanClassDefine> getBeanClassSet() {
        return beanDefineClassMap.values();
    }


    public List<Class<?>> getBeanClassOfAnnotation(Class<? extends Annotation> clazz) {
        return beanDefineClassMap.values().stream().filter(beanClassDefine -> beanClassDefine.getBeanClass().isAnnotationPresent(clazz)).map(BeanClassDefine::getBeanClass).collect(Collectors.toList());
    }

    /**
     * 获取bean的名称
     *
     * @param clazz
     * @return
     */
    protected String getBeanName(Class<?> clazz) {

        if (clazz.isInterface()) {
            List<BeanClassDefine> typeList = getBeanClassOfType(clazz);
            if (CollectionUtil.isEmpty(typeList)) {
                return clazz.getName();
            } else if (typeList.size() > 1) {
                String msg = String.format("该接口[%s]的实现类有%d个，应该只能是1个", clazz.getName(), typeList.size());
                throw new RuntimeException(msg);
            } else {
                clazz = typeList.get(0).getBeanClass();
            }
        }

        String name = clazz.getName();
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            name = StrUtil.isBlank(component.value()) ? name : component.value();
        } else if (clazz.isAnnotationPresent(Controller.class)) {
            Controller controller = clazz.getAnnotation(Controller.class);
            name = StrUtil.isBlank(controller.value()) ? name : controller.value();
        } else if (clazz.isAnnotationPresent(Configuration.class)) {
            Configuration configuration = clazz.getAnnotation(Configuration.class);
            name = StrUtil.isBlank(configuration.value()) ? name : configuration.value();
        }

        return name;

    }

}
