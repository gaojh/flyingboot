package com.github.gaojh.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.setting.dialect.Props;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Properties;

/**
 * @author 高建华
 * @date 2019-03-31 16:42
 */
public class Environment {

    private Props props;

    public Environment() {
        //先从配置文件加载
        URL url = ClassUtil.getResourceURL("application.properties");
        if (url != null) {
            props = new Props(url, Charset.forName("UTF-8"));
        }else{
            props = new Props();
        }

        //再从系统配置项获取并覆盖
        initFromSystem();
        ApplicationConfig.PORT = getInteger("server.port", 2019);
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

    public Boolean getBoolean(String key) {
        return props.getBool(key);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return props.getBool(key, defaultValue);
    }
}
