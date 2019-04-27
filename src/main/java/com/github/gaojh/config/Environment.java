package com.github.gaojh.config;

import cn.hutool.setting.dialect.Props;

import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @author 高建华
 * @date 2019-03-31 16:42
 */
public class Environment {

    private Props props;

    public Environment() {
        //先从配置文件加载
        props = new Props("application.properties", Charset.forName("UTF-8"));
        //再从系统配置项获取并覆盖
        initFromSystem();
    }

    private void initFromSystem() {
        Properties properties = System.getProperties();
        properties.stringPropertyNames().forEach(key -> props.setProperty(key, properties.getProperty(key)));
    }

    public String getString(String key) {
        return props.getStr(key);
    }

    public Integer getInteger(String key) {
        return props.getInt(key);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return props.getInt(key, defaultValue);
    }

}