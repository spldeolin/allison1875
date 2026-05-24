package __NAMESPACE__.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Configuration
public class ThreadPoolConfig {

    @Value("${thread-pool.core-size:4}")
    private Integer coreSize;

    @Value("${thread-pool.maximum-size:8}")
    private Integer maximumSize;

    @Value("${thread-pool.queue-capacity:20}")
    private Integer queueCapacity;

    @Value("${thread-pool.keep-alive-seconds:60}")
    private Integer keepAliveSeconds;

    @Autowired
    private BeanFactory beanFactory;

    @Bean("globalThreadPool")
    public ExecutorService globalThreadPool() {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(coreSize, maximumSize, keepAliveSeconds, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity), new ThreadPoolExecutor.CallerRunsPolicy());
        tpe.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("global-%d").build());
        return new TraceableExecutorService(beanFactory, tpe);
    }

}
