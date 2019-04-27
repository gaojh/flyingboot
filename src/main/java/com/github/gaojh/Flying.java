package com.github.gaojh;

import com.github.gaojh.config.ApplicationConfig;
import com.github.gaojh.config.Environment;
import com.github.gaojh.context.ApplicationContext;
import com.github.gaojh.ioc.annotation.ComponentScan;
import com.github.gaojh.mvc.ApplicationRunner;
import com.github.gaojh.context.ApplicationUtil;
import com.github.gaojh.server.HttpServer;

import java.util.List;

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

        if (source.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = source.getAnnotation(ComponentScan.class);
            if (componentScan.value().length > 0) {
                ApplicationConfig.BASE_PACKAGE = componentScan.value();

            }
        } else {
            ApplicationConfig.BASE_PACKAGE = new String[]{source.getPackage().getName()};
        }

        ApplicationConfig.PORT = environment.getInteger("server.port", 2019);

        ApplicationContext applicationContext = new ApplicationContext(environment);
        ApplicationUtil.setApplicationContext(applicationContext);

        List<ApplicationRunner> applicationRunners = applicationContext.getApplicationRunners();
        applicationRunners.forEach(ApplicationRunner::run);

        HttpServer httpServer = new HttpServer();
        try {
            httpServer.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
