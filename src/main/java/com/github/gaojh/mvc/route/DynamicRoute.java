package com.github.gaojh.mvc.route;

import com.github.gaojh.mvc.annotation.RequestMethod;
import lombok.Data;

/**
 * 动态路由定义
 *
 * @author 高建华
 * @date 2019-04-29 21:16
 */
@Data
public class DynamicRoute {

    private String path;
    private RequestMethod requestMethod;
    private Class<? extends DynamicRouteHandler> handlerClass;

    private DynamicRoute(Builder builder) {
        this.path = builder.path;
        this.requestMethod = builder.requestMethod;
        this.handlerClass = builder.handlerClass;
    }

    public static class Builder {

        private String path;
        private RequestMethod requestMethod;
        private Class<? extends DynamicRouteHandler> handlerClass;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder requestMethod(RequestMethod requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder handler(Class<? extends DynamicRouteHandler> handlerClass) {
            this.handlerClass = handlerClass;
            return this;
        }

        public DynamicRoute build() {
            return new DynamicRoute(this);
        }

    }
}
