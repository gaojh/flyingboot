package com.github.gaojh.context;

import com.github.gaojh.config.ApplicationConfig;
import com.github.gaojh.config.Environment;
import com.github.gaojh.ioc.bean.BeanFactory;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 高建华
 * @date 2019-04-28 21:17
 */
public class ApplicationContext extends BeanFactory {

    private Environment environment;

    private ExecutorService executorService;

    public ApplicationContext(Environment environment) {
        super(environment);
        this.environment = environment;
        executorService = new ThreadPoolExecutor(ApplicationConfig.THREAD_POOL_CORE_SIZE,
                ApplicationConfig.THREAD_POOL_MAX_SIZE,
                ApplicationConfig.THREAD_POOL_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new DefaultThreadFactory("Flying-pool"));
    }

    /**
     * 获取通用连接池
     *
     * @return
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

}
