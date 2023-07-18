package cn.org.wangchangjiu.redis.web.limit;

import cn.org.wangchangjiu.redis.web.limit.aop.RedisRateLimitAspect;
import cn.org.wangchangjiu.redis.web.limit.api.ApiConfigResolver;
import cn.org.wangchangjiu.redis.web.limit.api.RedisLimitHandlerInterceptor;
import cn.org.wangchangjiu.redis.web.limit.api.RedisLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.web.config.QuerydslWebConfiguration;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@ConditionalOnProperty(value = "redis.util.limit.enable", havingValue = "true")
@EnableConfigurationProperties({ RedisLimitProperties.class })
@Slf4j
public class RedisLimitAutoConfiguration {

    @Bean("limitRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        //设置value的序列化方式为JSOn
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        //设置key的序列化方式为String
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public RedisScript redisRequestRateLimiterScript() {
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/scripts/request_rate_limiter.lua")));
        redisScript.setResultType(Number.class);
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

    @Bean(initMethod = "initMethod")
    @ConditionalOnMissingBean
    public ApiConfigResolver apiConfigResolver(@Autowired RedisLimitProperties redisLimitProperties){
        return new ApiConfigResolver(redisLimitProperties);
    }


    @Bean
    @ConditionalOnMissingBean
    public RedisRateLimiter redisRateLimiter(@Qualifier(value = "limitRedisTemplate") RedisTemplate<String, Object> redisTemplate, @Qualifier("redisRequestRateLimiterScript") RedisScript<Number> redisScript) {
        return new RedisRateLimiter(redisTemplate, redisScript);
    }


   @Configuration
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
