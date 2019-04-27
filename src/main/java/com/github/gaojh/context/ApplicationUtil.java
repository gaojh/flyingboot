package com.github.gaojh.context;

import com.github.gaojh.config.ApplicationConfig;
import com.github.gaojh.config.Environment;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 高建华
 * @date 2019-03-31 23:15
 */
public class ApplicationUtil {

    @Setter
    @Getter
    public static ApplicationContext applicationContext;

    @Getter
    @Setter
    public static ApplicationConfig applicationConfig;

    @Setter
    @Getter
    public static Environment environment;
}