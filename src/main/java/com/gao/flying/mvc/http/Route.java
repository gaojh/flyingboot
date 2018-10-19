package com.gao.flying.mvc.http;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author 高建华
 * @date 2018/6/7 下午10:12
 */
@Data
@AllArgsConstructor
public class Route {
    /**
     * controller的instance
     */
    private Class<?> type;
    private Object object;
    private Method method;
}
