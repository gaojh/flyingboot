package com.github.gaojh.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2018/7/6 下午3:20
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Autowired {
    String value() default "";
}
