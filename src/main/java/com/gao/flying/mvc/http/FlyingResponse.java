package com.gao.flying.mvc.http;

/**
 * @author 高建华
 * @date 2018/7/5 上午11:18
 */
public interface FlyingResponse {

    FlyingResponse success(boolean res);

    boolean success();

    FlyingResponse msg(String msg);

    String msg();

    FlyingResponse data(Object data);

    Object data();

    FlyingResponse exception(Exception e);

    Exception exception();
}
