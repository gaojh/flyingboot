package com.github.flyingboot.mvc;

import com.github.flyingboot.mvc.http.HttpRequest;
import com.github.flyingboot.mvc.http.HttpResponse;

/**
 * @author 高建华
 * @date 2019-01-06 21:40
 */
public class Mvcs {
    public static InheritableThreadLocal<HttpRequest> request = new InheritableThreadLocal<>();
    public static InheritableThreadLocal<HttpResponse> response = new InheritableThreadLocal<>();

}
