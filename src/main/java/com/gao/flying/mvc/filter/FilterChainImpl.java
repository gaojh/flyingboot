package com.gao.flying.mvc.filter;

import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * @author 高建华
 * @date 2019-01-02 10:46
 */
public abstract class FilterChainImpl implements FilterChain {

    private LinkedList<Filter> filters = new LinkedList<>();

    private CompletableFuture<FlyingResponse> result;

    @Override
    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    @Override
    public void addFilters(Collection<Filter> collection) {
        filters.addAll(collection);
    }

    @Override
    public void doFilter(FlyingRequest flyingRequest, FlyingResponse flyingResponse) {
        if (filters.size() > 0) {
            filters.pop().doFilter(flyingRequest, flyingResponse, this);
        } else {
            execute(flyingRequest, flyingResponse);
        }
    }

    @Override
    public CompletableFuture<FlyingResponse> getResult() {
        return result;
    }

    @Override
    public void setResult(CompletableFuture<FlyingResponse> result) {
        this.result = result;
    }

    public abstract void execute(FlyingRequest flyingRequest, FlyingResponse flyingResponse);

}
