package com.gao.flying.example.interceptor;

import com.gao.flying.mvc.annotation.Interceptor;
import com.gao.flying.mvc.http.HttpRequest;
import com.gao.flying.mvc.http.HttpResponse;
import com.gao.flying.mvc.interceptor.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 高建华
 * @date 2019-04-16 20:52
 */
@Interceptor(pathPatterns = {"/demo"})
public class Demo2Interceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(Demo2Interceptor.class);
    @Override
    public boolean preHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        logger.info("demo2 preHandle");
        return true;
    }

    @Override
    public void postHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        logger.info("demo2 postHandle");
    }

    @Override
    public void afterCompletion(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        logger.info("demo2 afterCompletion");
    }
}
