package com.github.gaojh.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2019-01-15 13:15
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Interceptor {
    String[] pathPatterns();

    String[] ignorePathPatterns() default {};
}
