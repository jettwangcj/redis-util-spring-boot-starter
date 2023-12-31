package cn.org.wangchangjiu.redis;

import cn.org.wangchangjiu.redis.delay.DelayQueueAutoConfiguration;
import cn.org.wangchangjiu.redis.lock.RedisLockAutoConfiguration;
import cn.org.wangchangjiu.redis.mq.RedisStreamAutoConfiguration;
import cn.org.wangchangjiu.redis.util.RedisBloomUtils;
import cn.org.wangchangjiu.redis.util.StringRedisTemplateSliceUtils;
import cn.org.wangchangjiu.redis.web.duplicate.DuplicateSubmitAutoConfiguration;
import cn.org.wangchangjiu.redis.web.limit.RedisLimitAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Classname RedisUtilAutoConfiguration
 * @Description
 * @Date 2023/7/6 18:18
 * @Created by wangchangjiu
 */
@Configuration
@AutoConfigureAfter({ RedisAutoConfiguration.class, RedissonAutoConfiguration.class })
@Import(value = { DelayQueueAutoConfiguration.class,
        RedisLockAutoConfiguration.class,
        RedisStreamAutoConfiguration.class,
        DuplicateSubmitAutoConfiguration.class,
        RedisLimitAutoConfiguration.class
})
public class RedisUtilAutoConfiguration {


  @Bean
  @ConditionalOnMissingBean
  public RedisBloomUtils redisBloomUtil(@Autowired StringRedisTemplate redisTemplate){
    return new RedisBloomUtils(redisTemplate);
  }


  @Bean
  @ConditionalOnMissingBean
  public StringRedisTemplateSliceUtils stringRedisTemplateSliceUtils(@Autowired StringRedisTemplate redisTemplate){
    return new StringRedisTemplateSliceUtils(redisTemplate);
  }



}
