package cn.org.wangchangjiu.redis.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.util.ErrorHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Classname RedisStreamConfiguration
 * @Description
 * @Date 2023/7/3 21:48
 * @Created by wangchangjiu
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "redis.util.mq.enable", havingValue = "true" )
@EnableConfigurationProperties({ MyRedisStreamProperties.class })
public class RedisStreamAutoConfiguration {

    @Autowired
    private MyRedisStreamProperties myRedisStreamProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Bean
    @ConditionalOnMissingBean
    public RedisMessageProducer redisMessageProducer(){
        return new RedisMessageProducer(stringRedisTemplate);
    }

    @Bean(name = "redisMessageConsumerManager")
    @ConditionalOnMissingBean
    public RedisMessageConsumerManager redisMessageConsumerManager(){
        return new RedisMessageConsumerManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorHandler errorHandler(){
        return new StreamErrorHandler();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @DependsOn("redisMessageConsumerManager")
    @ConditionalOnMissingBean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(@Autowired RedisMessageConsumerManager redisMessageConsumerManager,
                                                                                                                    @Autowired RedisConnectionFactory redisConnectionFactory,
                                                                                                                    @Autowired ErrorHandler errorHandler) {
        MyRedisStreamProperties.Options options = myRedisStreamProperties.getOptions();
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        // 一次最多获取多少条消息
                        .batchSize(options.getBatchSize())
                        // 运行 Stream 的 poll task
                        .executor(getStreamMessageListenerExecutor())
                        // Stream 中没有消息时，阻塞多长时间，需要比 `spring.redis.timeout` 的时间小
                        .pollTimeout(options.getPollTimeout())
                        // 获取消息的过程或获取到消息给具体的消息者处理的过程中，发生了异常的处理
                        .errorHandler(errorHandler)
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory, containerOptions);

        // 获取 被 RedisMessageListener 注解修饰的 bean
        Map<String, RedisMessageConsumerContainer> consumerContainerGroups =
                redisMessageConsumerManager.getConsumerContainerGroups();

        // 循环遍历，创建 消费组
        consumerContainerGroups.forEach((groupQueue, redisMessageConsumerContainer) -> {
            String[] groupQueues = groupQueue.split("#");

            // 创建消费组
            createGroups(groupQueues);

            RedisMessageListener redisMessageListener = redisMessageConsumerContainer.getRedisMessageListener();
            if(!redisMessageListener.useGroup()){
                // 独立消费 不使用组
                streamMessageListenerContainer.receive(StreamOffset.fromStart(groupQueues[1]), new DefaultGroupStreamListener(redisMessageConsumerContainer));
            } else {
                // 消费组 消费
                if(redisMessageListener.autoAck()){
                    // 自动ACK
                    streamMessageListenerContainer.receiveAutoAck(Consumer.from(groupQueues[0], "consumer:" + UUID.randomUUID()),
                            StreamOffset.create(groupQueues[1], ReadOffset.lastConsumed()), new DefaultGroupStreamListener(redisMessageConsumerContainer));
                } else {
                    // 手动 ACK
                    streamMessageListenerContainer.receive(Consumer.from(groupQueues[0], "consumer:" + UUID.randomUUID()),
                            StreamOffset.create(groupQueues[1], ReadOffset.lastConsumed()), new DefaultGroupStreamListener(redisMessageConsumerContainer));
                }
            }
        });
        return streamMessageListenerContainer;
    }

    /**
     *  创建消费组
     * @param groupQueues
     */
    private void createGroups(String[] groupQueues) {
        // 判断是否存在队列Key
        if (stringRedisTemplate.hasKey(groupQueues[1])) {
            // 获取消费组 没有则创建
            StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(groupQueues[1]);
            if (groups.isEmpty()) {
                stringRedisTemplate.opsForStream().createGroup(groupQueues[1], groupQueues[0]);
            } else {
                AtomicBoolean exists= new AtomicBoolean(false);
                groups.forEach(xInfoGroup -> {
                    if (xInfoGroup.groupName().equals(groupQueues[0])){
                        exists.set(true);
                    }
                });
                if(!exists.get()){
                    stringRedisTemplate.opsForStream().createGroup(groupQueues[1], groupQueues[0]);
                }
            }
        } else {
            stringRedisTemplate.opsForStream().createGroup(groupQueues[1], groupQueues[0]);
        }
    }

    private Executor getStreamMessageListenerExecutor() {
        AtomicInteger index = new AtomicInteger(1);
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processors, processors, 0, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("async-stream-consumer-" + index.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
        return executor;
    }

}
