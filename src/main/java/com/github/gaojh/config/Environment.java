package com.github.gaojh.config;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.setting.dialect.Props;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 高建华
 * @date 2019-03-31 16:42
 */
public class Environment {

    /**
     * 基础包路径
     */
    @Getter
    @Setter
    private String[] baseScanPackages;

    /**
     * 默认web和websocket端口
     */
    @Getter
    @Setter
    private int port = 8080;

    @Getter
    @Setter
    private boolean enableWebsocket = false;

    private Props props;

    @Getter
    private ExecutorService executorService;

    public Environment() {
        //先从配置文件加载
        URL url = ClassUtil.getResourceURL(System.getProperty("flying.application", "application.properties"));
        if (url != null) {
            props = new Props(url, StandardCharsets.UTF_8);
        } else {
            props = new Props();
        }

        //再从系统配置项获取并覆盖
        initFromSystem();

        this.port = getInteger("server.port", 8080);
        this.enableWebsocket = getBoolean("flying.websocket.enable", false);
        /**
         * 连接池相关
         */
        Integer threadPoolCoreSize = getInteger("flying.thread.core.size", 600);
        Integer threadPoolMaxSize = getInteger("flying.thread.max.size", 2000);
        Long threadPoolKeepAliveTime = getLong("flying.thread.keepalive.time", 0L);

        //建立全局线程池
        executorService = new ThreadPoolExecutor(threadPoolCoreSize, threadPoolMaxSize, threadPoolKeepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<>(), new DefaultThreadFactory("flying-pool"));
    }

    private void initFromSystem() {
        Properties properties = System.getProperties();
        properties.stringPropertyNames().forEach(key -> props.setProperty(key, properties.getProperty(key)));
    }

    public String getString(String key) {
        return props.getStr(key);
    }

    public String getString(String key, String defaultValue) {
        return props.getStr(key, defaultValue);
    }

    public Integer getInteger(String key) {
        return props.getInt(key);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return props.getInt(key, defaultValue);
    }

    public Long getLong(String key) {
        return props.getLong(key);
    }

    public Long getLong(String key, Long value) {
        return props.getLong(key, value);
    }

    public Double getDouble(String key) {
        return props.getDouble(key);
    }

    public Double getDouble(String key, Double value) {
        return props.getDouble(key, value);
    }

    public Boolean getBoolean(String key) {
        return props.getBool(key);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return props.getBool(key, defaultValue);
    }
}
