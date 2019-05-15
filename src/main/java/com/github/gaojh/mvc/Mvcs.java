package com.github.gaojh.mvc;

import com.github.gaojh.server.http.HttpRequest;
import com.github.gaojh.server.http.HttpResponse;

/**
 * @author 高建华
 * @date 2019-01-06 21:40
 */
public class Mvcs {
    public static InheritableThreadLocal<HttpRequest> request = new InheritableThreadLocal<>();
    public static InheritableThreadLocal<HttpResponse> response = new InheritableThreadLocal<>();

}
