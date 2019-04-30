package com.github.gaojh.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2019-04-28 11:22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
    String[] value() default {};
}
