package com.github.gaojh.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2018/6/1 下午2:10
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequestMapping {
    String[] value() default {};
    RequestMethod[] method() default {};
}
