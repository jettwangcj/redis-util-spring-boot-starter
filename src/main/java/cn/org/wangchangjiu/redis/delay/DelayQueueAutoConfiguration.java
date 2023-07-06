package cn.org.wangchangjiu.redis.delay;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @Classname DelayQueueAutoConfig
 * @Description
 * @Date 2022/9/14 10:20
 * @Created by wangchangjiu
 */
@Configuration
@ConditionalOnBean({ RedissonClient.class })
@EnableConfigurationProperties({ RedissonDelayProperties.class })
@ConditionalOnProperty(value = "redis.util.delay.enable", havingValue = "true" )
public class DelayQueueAutoConfiguration {


    @Autowired
    private RedissonDelayProperties properties;

    @Autowired
    private RedissonClient redisson;

    @Bean
    @ConditionalOnMissingBean(DelayQueueMessage.class)
    public DelayQueueMessage delayQueueMessage(){
        return new RedissonDelayQueue(redisson);
    }


    @Bean
    @ConditionalOnMissingBean(DelayQueueMessageProducer.class)
    public DelayQueueMessageProducer delayQueueMessageProducer(){
        return new DelayQueueMessageProducer(delayQueueMessage(), properties.getRegisterService());
    }

    @Bean
    @ConditionalOnMissingBean(DelayQueueMessageConsumer.class)
    @DependsOn({"delayQueueMessage"})
    public DelayQueueMessageConsumer delayQueueMessageConsumer(){
        return new DelayQueueMessageConsumer(delayQueueMessage(), properties.getRegisterService());
    }

}
