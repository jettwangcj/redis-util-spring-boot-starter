package cn.org.wangchangjiu.redis.web.limit;

import cn.org.wangchangjiu.redis.web.limit.aop.RedisRateLimitAspect;
import cn.org.wangchangjiu.redis.web.limit.api.RedisLimitHandlerInterceptor;
import cn.org.wangchangjiu.redis.web.limit.api.RedisLimitProperties;
import cn.org.wangchangjiu.redis.web.limit.api.ApiConfigResolver;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
@ConditionalOnProperty(value = "redis.util.limit.enable", havingValue = "true")
@ConditionalOnBean({ StringRedisTemplate.class })
public class RedisLimitAutoConfiguration {

    @Bean
    public RedisScript redisRequestRateLimiterScript() {
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/scripts/request_rate_limiter.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({ Aspect.class })
    public RedisRateLimitAspect redisRateLimitAspect(@Autowired RedisRateLimiter redisRateLimiter){
        return new RedisRateLimitAspect(redisRateLimiter);
    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({ HandlerInterceptor.class })
    public RedisLimitHandlerInterceptor redisLimitHandlerInterceptor(@Autowired RedisRateLimiter redisRateLimiter,
                                                                     @Autowired ApiConfigResolver apiConfigResolver){
        return new RedisLimitHandlerInterceptor(redisRateLimiter, apiConfigResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(value = RedisLimitProperties.class )
    public ApiConfigResolver requestConfigResolver(@Autowired RedisLimitProperties redisLimitProperties){
        return new ApiConfigResolver(redisLimitProperties);
    }


    @Bean
    @ConditionalOnMissingBean
    public RedisRateLimiter redisRateLimiter(@Autowired StringRedisTemplate redisTemplate, @Qualifier("redisRequestRateLimiterScript") RedisScript<Long> redisScript) {
        return new RedisRateLimiter(redisTemplate, redisScript);
    }



}
