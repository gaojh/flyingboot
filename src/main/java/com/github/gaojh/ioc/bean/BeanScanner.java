package com.github.gaojh.ioc.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.StrUtil;
import com.github.gaojh.config.Environment;
import com.github.gaojh.ioc.annotation.Component;
import com.github.gaojh.ioc.annotation.Configuration;
import com.github.gaojh.mvc.annotation.Controller;
import com.github.gaojh.mvc.annotation.Interceptor;
import com.github.gaojh.mvc.annotation.Setup;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 高建华
 * @date 2019-04-28 21:25
 */
public class BeanScanner implements Scanner {

    private ConcurrentHashMap<String, Class<?>> beanClassMap = new ConcurrentHashMap<>();
    private ConcurrentHashSet<Class<?>> beanClassSet = new ConcurrentHashSet<>();

    public BeanScanner() {
        List<Class<? extends Annotation>> annotationList = CollUtil.toList(Component.class, Controller.class, Configuration.class, Setup.class, Interceptor.class);
        beanClassSet.addAll(scan(annotationList));
        beanClassSet.forEach(clazz -> beanClassMap.put(getBeanName(clazz), clazz));
    }

    protected Class<?> getBeanClass(String name) {
        return beanClassMap.get(name);
    }

    private List<Class<?>> getBeanClassOfType(Class<?> clazz) {
        return beanClassSet.stream().filter(c -> c.isAssignableFrom(clazz)).collect(Collectors.toList());
    }

    protected Set<Class<?>> getBeanClassSet(){
        return beanClassSet;
    }

    public List<Class<?>> getBeanClassOfAnnotation(Class<? extends Annotation> clazz){
        return beanClassSet.stream().filter(c -> c.isAnnotationPresent(clazz)).collect(Collectors.toList());
    }
    /**
     * 获取bean的名称
     *
     * @param clazz
     * @return
     */
    protected String getBeanName(Class<?> clazz) {

        if (clazz.isInterface()) {
            List<Class<?>> typeList = getBeanClassOfType(clazz);
            if (CollectionUtil.isEmpty(typeList)) {
                throw new RuntimeException(String.format("该接口[%s]未找到对应的实现类，或其实现类未加注解！", clazz.getName()));
            } else if (typeList.size() > 1) {
                String msg = String.format("该接口[%s]的实现类有%d个，应该只能是1个", clazz.getName(), typeList.size());
                throw new RuntimeException(msg);
            } else {
                clazz = typeList.get(0);
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
