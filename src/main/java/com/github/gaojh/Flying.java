package com.github.gaojh;

import com.github.gaojh.config.ApplicationConfig;
import com.github.gaojh.ioc.context.ApplicationContext;
import com.github.gaojh.ioc.context.ApplicationUtil;
import com.github.gaojh.mvc.context.WebContext;
import com.github.gaojh.server.HttpServer;

/**
 * @author 高建华
 * @date 2018/7/6 下午2:03
 */
public final class Flying {

    public static void run(Class<?> source) {

        if (source == null) {
            throw new RuntimeException("启动类为空");
        }

        ApplicationConfig.init(source);
        ApplicationContext applicationContext = new ApplicationContext();

        WebContext webContext = new WebContext(applicationContext);
        webContext.initWebContext();

        HttpServer httpServer = new HttpServer();
        try {
            httpServer.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
