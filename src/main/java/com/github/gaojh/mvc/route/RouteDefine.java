package com.github.gaojh.mvc.route;

import com.github.gaojh.mvc.annotation.RequestMethod;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;

/**
 * @author 高建华
 * @date 2018/6/7 下午10:12
 */
@Data
@Accessors(chain = true)
public class RouteDefine {

    private Class<?> type;
    /**
     * controller的instance
     */
    private Object object;
    private Method method;
    private String path;
    private RequestMethod[] requestMethod;

    private String[] paramNames;
    private Object[] params;
}
