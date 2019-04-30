package com.github.gaojh;

import com.github.gaojh.config.ApplicationConfig;
import com.github.gaojh.config.Environment;
import com.github.gaojh.context.ApplicationContext;
import com.github.gaojh.context.ApplicationUtil;
import com.github.gaojh.mvc.context.WebContext;
import com.github.gaojh.server.HttpServer;

/**
 * @author 高建华
 * @date 2018/7/6 下午2:03
 */
public class Flying {

    public static void run(Class<?> source) {

        if (source == null) {
            throw new RuntimeException("启动类为空");
        }

        Environment environment = new Environment();
        ApplicationConfig.init(source,environment);
        ApplicationContext applicationContext = new ApplicationContext(environment);
        ApplicationUtil.setApplicationContext(applicationContext);

        WebContext webContext = new WebContext(applicationContext);
        ApplicationUtil.setWebContext(webContext);
        webContext.initWebContext();

        HttpServer httpServer = new HttpServer();
        try {
            httpServer.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
