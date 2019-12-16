package com.github.gaojh.mvc.interceptor;

import com.github.gaojh.mvc.annotation.Interceptor;
import com.github.gaojh.mvc.utils.PathMatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author gaojianhua
 * @date 2019/12/11 3:57 下午
 */
public class InterceptorContext {

    private static ConcurrentHashMap<Interceptor, HandlerInterceptor> interceptorMap = new ConcurrentHashMap<>(128);

    public static List<HandlerInterceptor> getInterceptor(String path) {
        return interceptorMap.entrySet().stream()
                .filter(entry -> Arrays.stream(entry.getKey().pathPatterns()).anyMatch(pattern -> PathMatcher.me.match(pattern, path)))
                .filter(entry -> Arrays.stream(entry.getKey().ignorePathPatterns()).noneMatch(s -> PathMatcher.me.match(s, path)))
                .sorted(Comparator.comparingInt(value -> value.getKey().order()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public static void addInterceptor(Interceptor interceptor, HandlerInterceptor handlerInterceptor) {
        interceptorMap.put(interceptor, handlerInterceptor);
    }

}
