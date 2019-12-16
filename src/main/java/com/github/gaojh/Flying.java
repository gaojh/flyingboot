package com.github.gaojh;

import com.github.gaojh.config.Environment;
import com.github.gaojh.ioc.annotation.ComponentScan;
import com.github.gaojh.ioc.context.AppContext;
import com.github.gaojh.ioc.context.AppUtil;
import com.github.gaojh.mvc.context.WebContext;
import com.github.gaojh.server.HttpServer;
import com.github.gaojh.starter.StarterContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 高建华
 * @date 2018/7/6 下午2:03
 */
@Slf4j
public final class Flying {

    private AppContext appContext;

    private Environment environment;

    private volatile boolean started;

    public Flying() {
        environment = new Environment();
    }

    private void initBasic(Class<?> source) throws Exception {
        if (source.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = source.getAnnotation(ComponentScan.class);
            if (componentScan.value().length > 0) {
                environment.setBaseScanPackages(componentScan.value());
            }
        } else {
            environment.setBaseScanPackages(new String[]{source.getPackage().getName()});
        }
    }

    private void initContext() throws Exception {
        appContext = new AppContext(environment);
        WebContext webContext = new WebContext(appContext);

        AppUtil.appContext = appContext;
        AppUtil.webContext = webContext;

    }

    private void initStarters() throws Exception {
        StarterContext starterContext = new StarterContext(appContext);
        starterContext.scanStarter();
    }

    public Flying port(int port){
        this.environment.setPort(port);
        return this;
    }

    public Flying enableWebsocket(boolean enable){
        this.environment.setEnableWebsocket(enable);
        return this;
    }

    public void run(Class<?> source) {
        if (started) {
            log.error("服务已经启动，不能重复启动");
            return;
        }

        if (source == null) {
            throw new RuntimeException("启动类为空");
        }

        try {
            initBasic(source);
            initContext();
            initStarters();

            HttpServer httpServer = new HttpServer(environment);
            httpServer.start();
            started = true;
        } catch (Exception e) {
            log.error("启动失败", e);
            System.exit(0);
        }
    }

}
