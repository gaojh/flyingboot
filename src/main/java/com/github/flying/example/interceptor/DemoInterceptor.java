package com.github.flying.example.interceptor;

import com.github.flying.mvc.annotation.Interceptor;
import com.github.flying.mvc.http.HttpRequest;
import com.github.flying.mvc.http.HttpResponse;
import com.github.flying.mvc.interceptor.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 高建华
 * @date 2019-04-16 20:52
 */
@Interceptor(pathPatterns = {"/demo"})
public class DemoInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(DemoInterceptor.class);

    @Override
    public boolean preHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        logger.info("demo prehandle");
        return true;
    }

    @Override
    public void postHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        logger.info("demo postHandle");
    }

    @Override
    public void afterCompletion(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        logger.info("demo afterCompletion");
    }
}
