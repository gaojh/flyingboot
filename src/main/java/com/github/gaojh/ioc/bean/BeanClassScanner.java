package com.github.gaojh.ioc.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.github.gaojh.config.Environment;
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
public class BeanClassScanner implements ClassScanner {

    private ConcurrentHashMap<String, ClassDefine> classDefineMap = new ConcurrentHashMap<>();

    private Environment environment;

    public BeanClassScanner(Environment environment) {
        this.environment = environment;
        scan();
    }

    private void scan(){
        List<Class<? extends Annotation>> annotationList = CollUtil.toList(Configuration.class, Component.class, Controller.class, Setup.class, Interceptor.class);
        //扫描注解
        scanClass(annotationList).forEach(clazz -> {
            String beanName = getBeanName(clazz);
            classDefineMap.put(beanName, ClassDefine.builder().beanClass(clazz).beanName(beanName).isConfigurationBean(false).build());
            if (clazz.isAnnotationPresent(Configuration.class)) {
                Arrays.stream(clazz.getMethods()).filter(method -> method.isAnnotationPresent(Bean.class)).forEach(method -> {
                    String methodBeanName = getBeanName(method.getReturnType());
                    ClassDefine classDefine = ClassDefine.builder().beanName(methodBeanName).beanClass(method.getReturnType()).isConfigurationBean(true).configurationClass(clazz).method(method).build();
                    classDefineMap.put(methodBeanName, classDefine);
                });
            }
        });

    }

    @Override
    public ClassDefine getClassDefine(String name) {
        return classDefineMap.get(name);
    }

    @Override
    public List<ClassDefine> getClassDefineByInterface(Class<?> clazz) {
        return classDefineMap.values().stream().filter(classDefine -> clazz.isAssignableFrom(classDefine.getBeanClass())).collect(Collectors.toList());
    }

    @Override
    public Collection<ClassDefine> getClassDefines() {
        return classDefineMap.values();
    }

    @Override
    public List<Class<?>> getClassDefineByAnnotation(Class<? extends Annotation> clazz) {
        return classDefineMap.values().stream().filter(classDefine -> classDefine.getBeanClass().isAnnotationPresent(clazz)).map(ClassDefine::getBeanClass).collect(Collectors.toList());
    }

    /**
     * 获取bean的名称
     *
     * @param clazz
     * @return
     */
    @Override
    public String getBeanName(Class<?> clazz) {

        if (clazz.isInterface()) {
            List<ClassDefine> typeList = getClassDefineByInterface(clazz);
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


    /**
     * 扫描注解类
     *
     * @param classes 注解
     * @return 被注解的class
     */
    private Set<Class<?>> scanClass(List<Class<? extends Annotation>> classes) {
        if (classes == null || classes.size() == 0) {
            return new ConcurrentHashSet<>();
        }
        Set<Class<?>> beans = new ConcurrentHashSet<>();
        for (String basePkg : environment.getBaseScanPackages()) {
            for (Class<? extends Annotation> clazz : classes) {
                beans.addAll(ClassUtil.scanPackageByAnnotation(basePkg, clazz));
            }
        }

        return beans;
    }
}
