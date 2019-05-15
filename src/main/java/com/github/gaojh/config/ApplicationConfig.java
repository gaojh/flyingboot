package com.github.gaojh.config;

import com.github.gaojh.ioc.annotation.ComponentScan;

/**
 * @author 高建华
 * @date 2019-03-31 13:28
 */
public class ApplicationConfig {

    /**
     * 基础包路径
     */
    public static String[] BASE_PACKAGE;

    /**
     * 默认web端口
     */
    public static Integer PORT = 8080;

    /**
     * 连接池相关
     */
    public static Integer THREAD_POOL_CORE_SIZE = 600;
    public static Integer THREAD_POOL_MAX_SIZE = 2000;
    public static Long THREAD_POOL_KEEP_ALIVE_TIME = 0L;


    public static void init(Class<?> source) {
        if (source.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = source.getAnnotation(ComponentScan.class);
            if (componentScan.value().length > 0) {
                ApplicationConfig.BASE_PACKAGE = componentScan.value();
            }
        } else {
            ApplicationConfig.BASE_PACKAGE = new String[]{source.getPackage().getName()};
        }
    }


}
