package com.gao.flying.mvc.http;

/**
 * @author 高建华
 * @date 2018/6/24 下午8:26
 */
public class DefaultFlyingResponse implements FlyingResponse {

    private boolean success;
    private String msg;
    private Object data;
    private Exception exception;

    public static FlyingResponse buildResponse() {
        return new DefaultFlyingResponse();
    }

    public static FlyingResponse buildSuccess() {
        DefaultFlyingResponse response = new DefaultFlyingResponse();
        response.success = true;
        return response;
    }

    @Override
    public FlyingResponse success(boolean res) {
        this.success = res;
        return this;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public FlyingResponse msg(String msg) {
        this.msg = msg;
        return this;
    }

    @Override
    public String msg() {
        return msg;
    }

    @Override
    public FlyingResponse data(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public Object data() {
        return data;
    }

    @Override
    public FlyingResponse exception(Exception e) {
        this.exception = e;
        return this;
    }

    @Override
    public Exception exception() {
        return exception;
    }
}
