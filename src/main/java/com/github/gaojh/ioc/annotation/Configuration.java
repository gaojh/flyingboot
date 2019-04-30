package com.github.gaojh.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2019-04-28 11:17
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Configuration {
    String value() default "";
}
