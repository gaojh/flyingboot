package com.gao.flying.mvc.filter;

import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * @author 高建华
 * @date 2019-01-02 14:56
 */
public interface FilterChain {

    void addFilter(Filter filter);

    void addFilters(Collection<Filter> collection);

    void doFilter(FlyingRequest flyingRequest, FlyingResponse flyingResponse);

    CompletableFuture<FlyingResponse> getResult();

    void setResult(CompletableFuture<FlyingResponse> future);

}
