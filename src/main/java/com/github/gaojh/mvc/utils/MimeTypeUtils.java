package com.github.gaojh.mvc.utils;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 高建华
 * @date 2019-04-01 13:06
 */
public class MimeTypeUtils {

    private static Map<String, String> map = new HashMap<>();

    static {
        map.put("ico", "image/x-icon");
        map.put("jpeg", "image/jpeg");
        map.put("jpg", "image/jpegn");
        map.put("pdf", "application/pdf");
        map.put("png", "image/png");
        map.put("css", "text/css");
        map.put("js", "application/x-javascript");
        map.put("gif", "image/gif");
        map.put("bmp", "application/x-bmp");

        map.put("svg", "text/xml");
        map.put("xml", "text/xml");
        map.put("html", "text/html;charset=UTF-8");
    }

    public static String getContentType(String url) {
        if (StringUtils.contains(url, ".")) {
            String ext = StringUtils.substringAfterLast(url, ".").toLowerCase();
            String contentType = map.get(ext);
            if (StringUtils.isNotBlank(contentType)) {
                return contentType;
            }
        }
        return "text/plain;charset=UTF-8";
    }
}
