package com.github.gaojh.ioc.context;

import com.github.gaojh.config.Environment;
import com.github.gaojh.ioc.bean.BeanContext;

/**
 * @author 高建华
 * @date 2019-04-28 21:17
 */
public class AppContext extends BeanContext{

    private Environment environment;

    public AppContext(Environment environment){
        super(environment);
    }

    public Environment getEnvironment() {
        return getBean(Environment.class);
    }

    public BeanContext getBeanContext() {
        return AppContext.this;
    }

}
