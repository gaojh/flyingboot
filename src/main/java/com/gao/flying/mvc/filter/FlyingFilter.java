package com.gao.flying.mvc.filter;

import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;

/**
 * @author 高建华
 * @date 2018-12-15 16:10
 */
public interface FlyingFilter {

    boolean doFilter(FlyingRequest flyingRequest, FlyingResponse flyingResponse);
}
