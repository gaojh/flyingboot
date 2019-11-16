package com.github.gaojh.mvc.interceptor;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author gaojianhua
 * @date 2019/11/14 5:42 下午
 */
@Data
@Accessors(chain = true)
public class HandlerResponse {
    private boolean success;
    private String msg;

    public static HandlerResponse success() {
        return new HandlerResponse().setSuccess(true).setMsg("OK");
    }

    public static HandlerResponse fail(String msg) {
        return new HandlerResponse().setSuccess(false).setMsg(msg);
    }
}
