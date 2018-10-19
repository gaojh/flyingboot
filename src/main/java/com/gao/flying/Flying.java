package com.gao.flying;

import cn.hutool.setting.dialect.Props;
import com.gao.flying.netty.Server;
import com.gao.flying.context.ServerContext;

import java.nio.charset.Charset;

/**
 * @author 高建华
 * @date 2018/7/6 下午2:03
 */
public class Flying {

    public static void run(Class<?> source, String... args) {
        String basePackage = source.getPackage().getName();
        Props props = new Props("application.properties", Charset.forName("UTF-8"));
        if (!props.containsKey(FlyingConst.BASE_PACKAGE_STRING)) {
            props.put(FlyingConst.BASE_PACKAGE_STRING, basePackage);
        }
        ServerContext serverContext = new ServerContext(props);
        Server server = new Server(serverContext);
        try {
            server.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
