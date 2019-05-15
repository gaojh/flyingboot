package com.github.gaojh.mvc.route;

import cn.hutool.core.util.ClassUtil;
import com.github.gaojh.mvc.annotation.RequestMethod;
import com.github.gaojh.server.http.HttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 高建华
 * @date 2019-05-10 11:37
 */
public class RouterBuilder {

    private List<Route> routes = new ArrayList<>();

    public RouterBuilder add(String pattern, RequestMethod requestMethod, RouterHandler routerHandler) {
        routes.add(Route.builder().method(ClassUtil.getDeclaredMethod(routerHandler.getClass(), "handle", HttpRequest.class))
                .type(routerHandler.getClass())
                .requestMethod(new RequestMethod[]{requestMethod})
                .urlMapping(pattern)
                .object(routerHandler)
                .params(new Object[1]).build());
        return this;
    }

    public RouterBuilder POST(String pattern, RouterHandler routerHandler) {
        return add(pattern, RequestMethod.POST, routerHandler);
    }

    public RouterBuilder GET(String pattern, RouterHandler routerHandler) {
        return add(pattern, RequestMethod.GET, routerHandler);
    }


    public RouterFunction build() {
        return new RouterFunction(routes);
    }
}
