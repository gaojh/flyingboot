package cn.coinslab.flying;

import cn.coinslab.flying.config.ApplicationConfig;
import cn.coinslab.flying.config.ApplicationEnvironment;
import cn.coinslab.flying.context.ApplicationContext;
import cn.coinslab.flying.ioc.annotation.ComponentScan;
import cn.coinslab.flying.mvc.ApplicationRunner;
import cn.coinslab.flying.context.ApplicationUtil;
import cn.coinslab.flying.server.HttpServer;

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

        ApplicationEnvironment environment = new ApplicationEnvironment();

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