package com.fa.cim.frameworks.pprocess;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>PostProcessThreadPoolConfig .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/7/14 15:53    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/7/14 15:53
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "mycim.postprocess.pool", ignoreUnknownFields = false)
@Setter
public class PostProcessThreadPoolConfig {
    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 线程池维护线程的最大数量,只有在缓冲队列满了之后才会申请超过核心线程数的线程
     */
    private int maxPoolSize;

    /**
     * 缓存队列
     */
    private int queueCapacity;

    /**
     * 允许的空闲时间,当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
     */
    private int keepAlive;

    /**
     * 线程优先级
     */
    private int threadPriority;

    @Bean("postProcessExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (log.isInfoEnabled()) {
            log.info(">>> System available processors: {}", availableProcessors);
        }
        availableProcessors *= 1.2;
        if (log.isInfoEnabled()) {
            log.info(">>> Specific available processors: {}", availableProcessors);
        }

        //核心线程数
        taskExecutor.setCorePoolSize(corePoolSize > 0 ? corePoolSize : availableProcessors);
        //线程池维护线程的最大数量,只有在缓冲队列满了之后才会申请超过核心线程数的线程
        taskExecutor.setMaxPoolSize(maxPoolSize > 0 ? maxPoolSize : availableProcessors * 50);
        //缓存队列
        taskExecutor.setQueueCapacity(queueCapacity > 0 ? queueCapacity : availableProcessors * 200);
        //允许的空闲时间,当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        taskExecutor.setKeepAliveSeconds(keepAlive > 0 ? keepAlive : 200);
        //异步方法内部线程名称
        taskExecutor.setThreadNamePrefix("PostProcess-Thread-Pool--");
        //线程优先级
        taskExecutor.setThreadPriority(threadPriority > 0 && threadPriority < 10 ? threadPriority : 8);
        /*
         * 当线程池的任务缓存队列已满并且线程池中的线程数目达到maximumPoolSize，如果还有任务到来就会采取任务拒绝策略
         * 通常有以下四种策略：
         * ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。
         * ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
         * ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
         * ThreadPoolExecutor.CallerRunsPolicy：重试添加当前的任务，自动重复调用 execute() 方法，直到成功
         */
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }

}
