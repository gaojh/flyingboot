package com.github.gaojh.mvc.route;

import cn.hutool.core.util.ArrayUtil;
import com.github.gaojh.mvc.ApplicationRunner;
import com.github.gaojh.mvc.annotation.RequestMethod;
import com.github.gaojh.mvc.interceptor.HandlerInterceptor;
import com.github.gaojh.mvc.utils.PathMatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 高建华
 * @date 2019-04-29 15:10
 */
public class WebFactory {

    private ConcurrentHashMap<String, WebRoute> routeMap = new ConcurrentHashMap<>(128);
    private ConcurrentHashMap<String, List<HandlerInterceptor>> interceptorMap = new ConcurrentHashMap<>(128);
    private TreeMap<Integer, ApplicationRunner> setupMap = new TreeMap<>();


    public WebRoute getRoute(String url) {
        return this.getRoute(url, null);
    }

    public WebRoute getRoute(String url, RequestMethod method) {
        WebRoute webRoute = routeMap.get(url);
        if (webRoute == null) {
            Optional<WebRoute> optional = routeMap.entrySet().stream().filter(entry -> PathMatcher.me.isPattern(entry.getKey()) && PathMatcher.me.match(entry.getKey(), url)).map(Map.Entry::getValue).findFirst();
            if (optional.isPresent()) {
                webRoute = optional.get();
            } else {
                return null;
            }
        }

        if (method == null) {
            return webRoute;
        } else if (ArrayUtil.contains(webRoute.getRequestMethod(), method)) {
            return webRoute;
        } else {
            return null;
        }

    }

    protected void putRoute(String url, WebRoute webRoute) {
        this.routeMap.put(url, webRoute);
    }

    public List<HandlerInterceptor> getInterceptor(String path) {
        List<HandlerInterceptor> list = interceptorMap.get(path);
        if (list == null) {
            Optional<List<HandlerInterceptor>> optional = interceptorMap.entrySet().stream().filter(entry -> PathMatcher.me.isPattern(entry.getKey()) && PathMatcher.me.match(entry.getKey(), path)).map(Map.Entry::getValue).findFirst();
            list = optional.orElse(Collections.emptyList());
        }
        return list;
    }

    protected void putInterceptor(String path, HandlerInterceptor handlerInterceptor) {
        List<HandlerInterceptor> list = interceptorMap.getOrDefault(path, new ArrayList<>());
        list.add(handlerInterceptor);
        interceptorMap.put(path, list);
    }


    protected void putApplicationRunner(Integer order, ApplicationRunner applicationRunner) {
        this.setupMap.put(order, applicationRunner);
    }

    public List<ApplicationRunner> getApplicationRunners() {
        Set<Integer> keys = setupMap.keySet();
        List<ApplicationRunner> list = new ArrayList<>();
        for (Integer idx : keys) {
            list.add(setupMap.get(idx));
        }
        return list;
    }
}
