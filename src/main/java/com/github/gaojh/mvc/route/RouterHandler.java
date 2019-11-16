package com.github.gaojh.mvc.route;


import com.github.gaojh.server.http.HttpRequest;

import java.util.Map;

/**
 * 动态路由处理器
 *
 * @author 高建华
 * @date 2019-04-30 09:38
 */
@FunctionalInterface
public interface RouterHandler {

    Object handle(HttpRequest httpRequest);
}
