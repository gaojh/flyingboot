package com.github.gaojh.ioc.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.gaojh.config.Environment;
import com.github.gaojh.ioc.annotation.Autowired;
import com.github.gaojh.ioc.annotation.Bean;
import com.github.gaojh.ioc.annotation.Configuration;
import com.github.gaojh.ioc.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BeanCreator extends BeanScanner {

    private static final Logger logger = LoggerFactory.getLogger(BeanCreator.class);

    private final ConcurrentHashMap<String, BeanDefine> beanDefineMap = new ConcurrentHashMap<>();

    private Environment environment;

    public BeanCreator(Environment environment) {
        this.environment = environment;
        this.createBeanDefine(environment);
        initBeans();
    }

    protected BeanDefine getBeanDefine(String name) {
        return beanDefineMap.get(name);
    }

    protected BeanDefine getBeanDefine(Class<?> clazz) {
        return beanDefineMap.get(getBeanName(clazz));
    }

    protected BeanDefine createBeanDefine(Object object) {
        BeanDefine beanDefine = new BeanDefine(object);
        beanDefineMap.put(getBeanName(beanDefine.getType()), beanDefine);
        return beanDefine;
    }

    private void initBeans() {
        getBeanClassSet().forEach(clazz -> {
            try {
                createBeanDefine(clazz);
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
    private BeanDefine createBeanDefine(Class<?> clazz) throws Exception {
        String name = getBeanName(clazz);
        if (beanDefineMap.containsKey(name)) {
            return beanDefineMap.get(name);
        }
        Class<?> finalClass = getBeanClass(name);
        Constructor[] constructors = finalClass.getDeclaredConstructors();
        Object object;
        if (constructors.length == 0) {
            object = finalClass.newInstance();
        } else {
            Constructor constructor = getAutowriedContructor(finalClass);
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

        if (clazz.isAnnotationPresent(Configuration.class)) {
            initConfiguration(clazz);
        }
        logger.debug("加载bean：{}", name);
        return beanDefine;
    }

    /**
     * 初始化Configuration
     *
     * @param clazz
     */
    private void initConfiguration(Class<?> clazz) throws Exception {
        BeanDefine beanDefine = getBeanDefine(clazz);
        List<Method> beanMethods = Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(Bean.class)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(beanMethods)) {
            for (Method method : beanMethods) {
                Object object;
                if (method.getParameterCount() == 0) {
                    object = method.invoke(beanDefine.getObject());
                } else {
                    object = method.invoke(beanDefine.getObject(), getParameters(method));
                }
                logger.debug("从Configuration加载bean：{}", method.getReturnType().getName());
                createBeanDefine(object);
            }
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
    private Constructor getAutowriedContructor(Class clazz) {
        Constructor[] constructors = clazz.getDeclaredConstructors();

        List<Constructor> list = Arrays.stream(clazz.getConstructors()).filter(constructor -> constructor.isAnnotationPresent(Autowired.class)).collect(Collectors.toList());
        if (list.size() == 0) {
            for (Constructor constructor : constructors) {
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
     * 实例化构造方法入参
     *
     * @param constructor
     * @return
     */
   /* private Object[] getConstractorParameters(Constructor constructor) throws Exception {
        Class[] parameters = constructor.getParameterTypes();
        List<Object> values = new ArrayList<>(parameters.length);
        for (Class c : parameters) {
            String parameterName = getBeanName(c);
            BeanDefine beanDefine = beanDefineMap.get(parameterName);
            if (beanDefine == null) {
                beanDefine = createBeanDefine(c);
                values.add(beanDefine.getObject());
            }
        }
        return values.toArray();
    }*/


    /**
     * 实例化方法入参
     *
     * @param t
     * @return
     */
    private <T extends Executable> Object[] getParameters(T t) throws Exception {
        Class[] parameters = t.getParameterTypes();
        List<Object> values = new ArrayList<>(parameters.length);
        for (Class c : parameters) {
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
