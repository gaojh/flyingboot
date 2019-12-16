package com.github.gaojh.mvc.route;

import cn.hutool.core.util.ArrayUtil;
import com.github.gaojh.mvc.annotation.RequestMethod;
import com.github.gaojh.mvc.utils.PathMatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gaojianhua
 * @date 2019/12/11 3:00 下午
 */
@Slf4j
public class RouteContext {

    private static ConcurrentHashMap<String, RouteDefine> routeMap = new ConcurrentHashMap<>(200);

    public static RouteDefine getRoute(String url) {
        return getRoute(url, null);
    }

    public static RouteDefine getRoute(String url, RequestMethod method) {
        RouteDefine routeDefine = routeMap.get(url);
        if (routeDefine == null) {
            Optional<RouteDefine> optional = routeMap.entrySet().stream().filter(entry -> PathMatcher.me.isPattern(entry.getKey()) && PathMatcher.me.match(entry.getKey(), url)).map(Map.Entry::getValue).findFirst();
            if (optional.isPresent()) {
                routeDefine = optional.get();
            } else {
                return null;
            }
        }

        if (method == null) {
            return routeDefine;
        } else if (ArrayUtil.contains(routeDefine.getRequestMethod(), method)) {
            return routeDefine;
        } else {
            return null;
        }
    }

    public static void addRoute(String url, RouteDefine routeDefine) {
        log.debug("注册路由：{} ===> {}", url, routeDefine.getType().getName() + "." + routeDefine.getMethod().getName());
        routeMap.put(url, routeDefine);
    }

    public void remoteRoute(String url) {
        log.debug("删除路由：{}", url);
        routeMap.remove(url);
    }
}
