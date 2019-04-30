package com.github.gaojh.example;

import com.github.gaojh.context.ApplicationUtil;
import com.github.gaojh.example.handler.DemoDynamicHandler;
import com.github.gaojh.mvc.ApplicationRunner;
import com.github.gaojh.mvc.annotation.RequestMethod;
import com.github.gaojh.mvc.context.WebContext;
import com.github.gaojh.mvc.route.DynamicRoute;

/**
 * @author 高建华
 * @date 2019-04-30 10:32
 */
@com.github.gaojh.mvc.annotation.Setup
public class Setup implements ApplicationRunner {

    @Override
    public void run() {
        WebContext webContext = ApplicationUtil.getWebContext();
        DynamicRoute.Builder builder = new DynamicRoute.Builder();
        webContext.addDynamicRoute(builder.path("/test").requestMethod(RequestMethod.POST).handler(DemoDynamicHandler.class).build());
    }
}
