package com.github.gaojh.mvc.route;

import com.github.gaojh.mvc.annotation.RequestMethod;
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
public class WebRoute {
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

    public WebRoute(Class type, Object object, Method method, String urlMapping,RequestMethod[] requestMethod) {
        this.type = type;
        this.object = object;
        this.method = method;
        this.urlMapping = urlMapping;
        this.requestMethod = requestMethod;
    }
}
