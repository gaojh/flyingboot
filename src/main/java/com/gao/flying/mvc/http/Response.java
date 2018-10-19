package com.gao.flying.mvc.http;

/**
 * @author 高建华
 * @date 2018/7/5 上午11:18
 */
public interface Response {

    Response success(boolean res);

    boolean success();

    Response msg(String msg);

    String msg();

    Response data(Object data);

    Object data();

    Response exception(Exception e);

    Exception exception();
}
