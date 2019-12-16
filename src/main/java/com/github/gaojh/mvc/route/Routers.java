package com.github.gaojh.mvc.route;

import cn.hutool.core.util.ClassUtil;
import com.github.gaojh.mvc.annotation.RequestMethod;
import com.github.gaojh.server.http.HttpRequest;

/**
 * @author 高建华
 * @date 2019-05-10 11:42
 */
public enum Routers {
    /**
     * 单例
     */
    me;

    public Routers add(String path, RequestMethod requestMethod, DynamicRouteHandler dynamicRouteHandler) {
        RouteDefine routeDefine = new RouteDefine();
        routeDefine.setType(dynamicRouteHandler.getClass());
        routeDefine.setMethod(ClassUtil.getDeclaredMethod(dynamicRouteHandler.getClass(), "handle", HttpRequest.class));
        routeDefine.setObject(dynamicRouteHandler);
        routeDefine.setPath(path);
        routeDefine.setObject(new Object[1]);
        routeDefine.setRequestMethod(new RequestMethod[]{requestMethod});
        RouteContext.addRoute(path, routeDefine);
        return this;
    }

    public Routers post(String pattern, DynamicRouteHandler dynamicRouteHandler) {
        return add(pattern, RequestMethod.POST, dynamicRouteHandler);
    }

    public Routers get(String pattern, DynamicRouteHandler dynamicRouteHandler) {
        return add(pattern, RequestMethod.GET, dynamicRouteHandler);
    }

}
