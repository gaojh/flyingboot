package cn.coinslab.flying.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author 高建华
 * @date 2018/6/8 下午3:15
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.PARAMETER)
public @interface RequestBody {
}