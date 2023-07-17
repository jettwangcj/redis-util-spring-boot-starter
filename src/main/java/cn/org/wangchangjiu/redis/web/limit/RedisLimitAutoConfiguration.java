package cn.org.wangchangjiu.redis.web.limit;

import cn.org.wangchangjiu.redis.web.limit.aop.RedisRateLimitAspect;
import cn.org.wangchangjiu.redis.web.limit.api.RedisLimitHandlerInterceptor;
import cn.org.wangchangjiu.redis.web.limit.api.RedisLimitProperties;
import cn.org.wangchangjiu.redis.web.limit.api.ApiConfigResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


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
    @ConditionalOnBean({ ApiConfigResolver.class, RedisRateLimiter.class })
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

    @Configuration
    @ConditionalOnBean(value = RedisLimitHandlerInterceptor.class)
   public class MvcConfigurer implements WebMvcConfigurer {

        @Autowired
        private RedisLimitHandlerInterceptor redisLimitHandlerInterceptor;
        @Override
        public void addInterceptors(InterceptorRegistry registry) {

            registry.addInterceptor(redisLimitHandlerInterceptor).addPathPatterns("/**");
       }

   }

    /**
     *  内置 KeyResolver
     */
    public static class BuiltInKeyResolver {

        @Bean(name = "ipKeyResolver")
        @ConditionalOnMissingBean
        public KeyResolver ipKeyResolver(){
            return request -> getIpAddr(request);
        }

        @Bean(name = "apiKeyResolver")
        @ConditionalOnMissingBean
        public KeyResolver apiKeyResolver(){
            return request -> request.getRequestURI();
        }

        private static String getIpAddr(HttpServletRequest request) {
            String ipAddress;
            try {
                ipAddress = request.getHeader("x-forwarded-for");
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getRemoteAddr();
                }
                // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
                if (ipAddress != null && ipAddress.length() > 15) {
                    if (ipAddress.indexOf(",") > 0) {
                        ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                    }
                }
            } catch (Exception e) {
                ipAddress = "127.0.0.1";
            }
            return ipAddress;
        }


    }



}
