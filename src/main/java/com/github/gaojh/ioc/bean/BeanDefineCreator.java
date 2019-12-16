package com.github.gaojh.ioc.bean;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.gaojh.config.Environment;
import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.ioc.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 高建华
 * @date 2019-04-28 16:37
 */
@Slf4j
public class BeanDefineCreator extends BeanClassScanner implements BeanCreator {

    private final ConcurrentHashMap<String, BeanDefine> beanDefineMap = new ConcurrentHashMap<>();

    private Environment environment;

    public BeanDefineCreator(Environment environment) {
        super(environment);
        this.createBeanDefine(environment);
        initBeans();
    }

    @Override
    public BeanDefine getBeanDefine(String name) {
        return beanDefineMap.get(name);
    }

    @Override
    public BeanDefine getBeanDefine(Class<?> clazz) {
        return beanDefineMap.get(getBeanName(clazz));
    }

    @Override
    public BeanDefine createBeanDefine(Object object) {
        BeanDefine beanDefine = new BeanDefine(object);
        String name = getBeanName(beanDefine.getType());
        beanDefineMap.put(name, beanDefine);
        return beanDefine;
    }

    @Override
    public BeanDefine createBeanDefine(String name, Object object) {
        BeanDefine beanDefine = new BeanDefine(object);
        beanDefineMap.put(name, beanDefine);
        return beanDefine;
    }

    @Override
    public List<BeanDefine> getBeanDefineByAnnotation(Class<? extends Annotation> annotationClass) {
        return beanDefineMap.values().stream().filter(beanDefine -> beanDefine.getType().isAnnotationPresent(annotationClass)).collect(Collectors.toList());
    }

    private void initBeans() {
        getClassDefines().forEach(classDefine -> {
            try {
                createBeanDefine(classDefine.getBeanClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 对象实例化
     *
     * @param clazz
     * @return
     */
    public BeanDefine createBeanDefine(Class<?> clazz) throws Exception {
        String name = getBeanName(clazz);
        if (beanDefineMap.containsKey(name)) {
            return beanDefineMap.get(name);
        }
        ClassDefine classDefine = getClassDefine(name);
        if (classDefine == null) {
            throw new RuntimeException("未找到到该类，可能未加入IOC管理：" + name);
        }

        Object object;
        if (classDefine.isConfigurationBean()) {
            BeanDefine configurationBeanDefine = createBeanDefine(classDefine.getConfigurationClass());
            Method method = classDefine.getMethod();
            if (method.getParameterCount() == 0) {
                object = method.invoke(configurationBeanDefine.getObject());
            } else {
                object = method.invoke(configurationBeanDefine.getObject(), getParameters(method));
            }
            log.debug("加载Configuration Bean：{}", name);
            return createBeanDefine(classDefine.getBeanName(), object);
        } else {
            Class<?> finalClass = classDefine.getBeanClass();
            Constructor<?>[] constructors = finalClass.getConstructors();

            if (constructors.length == 0) {
                object = finalClass.newInstance();
            } else {
                Constructor<?> constructor = getAutowriedContructor(finalClass);
                if (constructor.getParameterCount() == 0) {
                    object = finalClass.newInstance();
                } else {
                    object = constructor.newInstance(getParameters(constructor));
                }
            }

            BeanDefine beanDefine = new BeanDefine(finalClass, object);
            //还未file设值，先放入临时的map中，设置完成之后再移入正式的
            beanDefineMap.put(name, beanDefine);
            //设置Field
            setFields(beanDefine);
            log.debug("加载Bean：{}", name);
            return beanDefine;
        }
    }


    /**
     * 获取构造函数，思路如下
     * 1、先判断是否有Autowried的构造函数，一个，则返回，多个报错
     * 2、再判断是否有无参构造函数，有则返回，无则报错
     *
     * @param clazz
     * @return
     */
    private Constructor<?> getAutowriedContructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        List<Constructor<?>> list = Arrays.stream(clazz.getConstructors()).filter(constructor -> constructor.isAnnotationPresent(Autowired.class)).collect(Collectors.toList());
        if (list.size() == 0) {
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    return constructor;
                }
            }
            throw new RuntimeException(String.format("类[%s]需要一个无参构造函数，否则无法实例化！", clazz.getName()));
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new RuntimeException(String.format("类[%s]只能存在一个使用@Autowried注解的构造方法！", clazz.getName()));
        }
    }

    /**
     * 实例化方法入参
     *
     * @param t
     * @return
     */
    private <T extends Executable> Object[] getParameters(T t) throws Exception {
        Class<?>[] parameters = t.getParameterTypes();
        List<Object> values = new ArrayList<>(parameters.length);
        for (Class<?> c : parameters) {
            String parameterName = getBeanName(c);
            BeanDefine beanDefine = beanDefineMap.get(parameterName);
            if (beanDefine == null) {
                beanDefine = createBeanDefine(c);
            }
            values.add(beanDefine.getObject());
        }
        return values.toArray();
    }

    /**
     * 设置Bean的Field
     *
     * @param beanDefine
     */
    private void setFields(BeanDefine beanDefine) throws Exception {
        for (Field field : beanDefine.getType().getDeclaredFields()) {

            if (field.isAnnotationPresent(Autowired.class)) {
                setAutowiredField(beanDefine, field);
            } else if (field.isAnnotationPresent(Value.class)) {
                setValueField(beanDefine, field);
            }
        }
    }

    private void setAutowiredField(BeanDefine beanDefine, Field field) throws Exception {
        String fieldName = getBeanName(field.getType());
        BeanDefine fieldBeanDefine = beanDefineMap.get(fieldName);
        if (fieldBeanDefine == null) {
            fieldBeanDefine = createBeanDefine(field.getType());
        }
        ReflectUtil.setFieldValue(beanDefine.getObject(), field, fieldBeanDefine.getObject());
    }

    private void setValueField(BeanDefine beanDefine, Field field) {
        Value value = field.getAnnotation(Value.class);
        String valueStr = value.value();
        if (StrUtil.isBlank(valueStr)) {
            throw new RuntimeException(String.format("[%s]中的Field：%s无法设值！", beanDefine.getType().getName(), field.getName()));
        } else if (!StrUtil.containsAny(valueStr, "${", "}")) {
            throw new RuntimeException(String.format("[%s]中的Field：%s表达式格式不正确！", beanDefine.getType().getName(), field.getName()));
        } else {
            String key = StrUtil.subBetween(valueStr, "${", "}");
            ReflectUtil.setFieldValue(beanDefine.getObject(), field, environment.getString(key));
        }
    }

}
