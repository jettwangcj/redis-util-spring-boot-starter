package cn.org.wangchangjiu.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Classname RedisLockAutoConfiguration
 * @Description redis lock 自动配置
 * @Date 2023/7/5 10:12
 * @Created by wangchangjiu
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "redis.util.lock.enable", havingValue = "true", matchIfMissing = true )
@ConditionalOnClass({ RedissonClient.class, Aspect.class})
public class RedisLockAutoConfiguration {

    @Bean
    @ConditionalOnBean({ RedissonClient.class })
    @ConditionalOnMissingBean
    public RedisLockAspect duplicateSubmitAspect(@Autowired RedissonClient redissonClient){
        return new RedisLockAspect(redissonClient);
    }



}
