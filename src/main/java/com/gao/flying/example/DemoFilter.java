package com.gao.flying.example;

import com.gao.flying.mvc.annotation.WebFilter;
import com.gao.flying.mvc.filter.Filter;
import com.gao.flying.mvc.filter.FilterChain;
import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author 高建华
 * @date 2018-12-15 16:17
 */
@WebFilter
public class DemoFilter implements Filter {

    @Override
    public void doFilter(FlyingRequest flyingRequest, FlyingResponse flyingResponse, FilterChain filterChain) {
        System.out.println("开始");
        //filterChain.doFilter(flyingRequest, flyingResponse);
        System.out.println("结束");
        flyingResponse.success(false).msg("被过滤").httpResponseStatus(HttpResponseStatus.BAD_REQUEST);
    }

}
