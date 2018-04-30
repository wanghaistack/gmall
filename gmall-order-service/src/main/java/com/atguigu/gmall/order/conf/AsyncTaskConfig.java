package com.atguigu.gmall.order.conf;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncTaskConfig implements AsyncConfigurer {
    @Override
    @Bean
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor=new ThreadPoolTaskExecutor();
        //设置线程数
        threadPoolTaskExecutor.setCorePoolSize(10);
        //最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(50);
        //保持活跃的秒数
        threadPoolTaskExecutor.setKeepAliveSeconds(5);
        //等待队列容量，线程数不够会等待
        threadPoolTaskExecutor.setQueueCapacity(100);
        //初始化
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;

    }

    @Override
    @Bean
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
