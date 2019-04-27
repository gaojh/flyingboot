package com.github.gaojh.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2019-03-30 23:01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ComponentScan {
    String[] value() default {};
}
