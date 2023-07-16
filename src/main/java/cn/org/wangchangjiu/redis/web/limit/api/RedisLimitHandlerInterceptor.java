package cn.org.wangchangjiu.redis.web.limit.api;

import cn.org.wangchangjiu.redis.web.limit.RedisRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class RedisLimitHandlerInterceptor implements HandlerInterceptor {

    private RedisRateLimiter redisRateLimiter;

    private ApiConfigResolver apiConfigResolver;

    public RedisLimitHandlerInterceptor(RedisRateLimiter redisRateLimiter, ApiConfigResolver apiConfigResolver){
        this.redisRateLimiter = redisRateLimiter;
        this.apiConfigResolver = apiConfigResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApiConfigResolver.Config config = this.apiConfigResolver.mathConfig(request.getRequestURI());
        if(config != null){
            boolean allowed = this.redisRateLimiter.isAllowed(config.getKeyResolver().resolve(request), config.getReplenishRate(), config.getBurstCapacity());
            if(!allowed){
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                return false;
            }
        }
        return true;
    }

}
