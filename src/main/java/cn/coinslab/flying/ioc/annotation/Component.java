package cn.coinslab.flying.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2018/7/6 下午3:17
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Component {
    String value() default "";
}
