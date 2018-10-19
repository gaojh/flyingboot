package com.gao.flying.mvc.http;

/**
 * @author 高建华
 * @date 2018/6/24 下午8:26
 */
public class DefaultResponse implements Response {

    private boolean success;
    private String msg;
    private Object data;
    private Exception exception;

    public static Response buildResponse() {
        return new DefaultResponse();
    }

    public static Response buildSuccess() {
        DefaultResponse response = new DefaultResponse();
        response.success = true;
        return response;
    }

    @Override
    public Response success(boolean res) {
        this.success = res;
        return this;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public Response msg(String msg) {
        this.msg = msg;
        return this;
    }

    @Override
    public String msg() {
        return msg;
    }

    @Override
    public Response data(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public Object data() {
        return data;
    }

    @Override
    public Response exception(Exception e) {
        this.exception = e;
        return this;
    }

    @Override
    public Exception exception() {
        return exception;
    }
}
