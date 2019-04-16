package com.gao.flying.config;

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
    public static Long THREAD_POOL_KEEPALIVETIME = 0L;


}
