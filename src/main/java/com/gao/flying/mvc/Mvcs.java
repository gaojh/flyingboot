package com.gao.flying.mvc;

import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;

/**
 * @author 高建华
 * @date 2019-01-06 21:40
 */
public class Mvcs {
    public static InheritableThreadLocal<FlyingRequest> request = new InheritableThreadLocal<>();
    public static InheritableThreadLocal<FlyingResponse> response = new InheritableThreadLocal<>();

}
