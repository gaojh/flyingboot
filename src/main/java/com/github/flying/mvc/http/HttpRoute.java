package com.github.flying.mvc.http;

import com.github.flying.mvc.annotation.RequestMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * @author 高建华
 * @date 2018/6/7 下午10:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpRoute {
    /**
     * controller的instance
     */
    private Class<?> type;
    private Object object;
    private Method method;
    private String urlMapping;
    private RequestMapping.METHOD httpMethod;

    private String[] paramNames;
    private Object[] params;

    public HttpRoute(Class type, Object object, Method method, String urlMapping) {
        this.type = type;
        this.object = object;
        this.method = method;
        this.urlMapping = urlMapping;
    }
}
