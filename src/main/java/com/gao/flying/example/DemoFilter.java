package com.gao.flying.example;

import com.gao.flying.mvc.annotation.Filter;
import com.gao.flying.mvc.filter.FlyingFilter;
import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;

/**
 * @author 高建华
 * @date 2018-12-15 16:17
 */
@Filter
public class DemoFilter implements FlyingFilter {

    @Override
    public boolean doFilter(FlyingRequest flyingRequest, FlyingResponse flyingResponse) {
        return false;
    }
}
