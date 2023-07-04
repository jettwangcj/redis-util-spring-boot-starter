package cn.org.wangchangjiu.redis.web.request;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @Classname DuplicateSubmitAutoConfiguration
 * @Description
 * @Date 2023/7/3 18:28
 * @Created by wangchangjiu
 */
@Configuration
@ConditionalOnProperty(value = "redis.util.duplicate.enable", havingValue = "true" )
@ConditionalOnClass({ RedisTemplate.class, Aspect.class})
public class DuplicateSubmitAutoConfiguration {

    @Bean
    public DuplicateSubmitAspect duplicateSubmitAspect(@Autowired RedisTemplate<Object, Object> redisTemplate){
        return new DuplicateSubmitAspect(redisTemplate);
    }


}
