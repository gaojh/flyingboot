package cn.coinslab.flying.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2018/7/9 下午2:10
 *
 * 加了此注解，在启动时调用其run方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Setup {
    int order() default 0;
}
