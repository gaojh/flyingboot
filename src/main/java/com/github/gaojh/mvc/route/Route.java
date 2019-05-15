package com.github.gaojh.mvc.route;

import com.github.gaojh.mvc.annotation.RequestMethod;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author 高建华
 * @date 2018/6/7 下午10:12
 */
@Data
@Builder
public class Route {
    /**
     * controller的instance
     */
    private Class<?> type;
    private Object object;
    private Method method;
    private String urlMapping;
    private RequestMethod[] requestMethod;

    private String[] paramNames;
    private Object[] params;
}
