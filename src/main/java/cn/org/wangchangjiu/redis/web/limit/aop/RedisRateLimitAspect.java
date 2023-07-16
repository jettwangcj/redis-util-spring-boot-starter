package cn.org.wangchangjiu.redis.web.limit.aop;

import cn.org.wangchangjiu.redis.common.MyAopUtil;
import cn.org.wangchangjiu.redis.web.limit.KeyResolver;
import cn.org.wangchangjiu.redis.web.limit.RedisLimitException;
import cn.org.wangchangjiu.redis.web.limit.RedisRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

public class RedisRateLimitAspect implements ApplicationContextAware {

    private RedisRateLimiter redisRateLimiter;

    private ApplicationContext applicationContext;

    private static Logger logger = LoggerFactory.getLogger(RedisRateLimitAspect.class);

    public RedisRateLimitAspect(RedisRateLimiter redisRateLimiter){
        this.redisRateLimiter = redisRateLimiter;
    }

    @Pointcut("@annotation(cn.org.wangchangjiu.redis.web.limit.aop.RedisRateLimitConfig)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method currentMethod = MyAopUtil.getCurrentMethod(joinPoint);
        if(currentMethod != null){
            RedisRateLimitConfig annotation = currentMethod.getAnnotation(RedisRateLimitConfig.class);
            if(annotation != null){
                KeyResolver keyResolver = applicationContext.getBean(annotation.keyResolver());
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                boolean allowed = redisRateLimiter.isAllowed(keyResolver.resolve(request), annotation.replenishRate(), annotation.burstCapacity());
                if(!allowed){
                    if(annotation.exceptionWithLimit()){
                        throw new RedisLimitException("limit .....");
                    } else {
                        Signature signature = joinPoint.getSignature();
                        Class returnType = ((MethodSignature) signature).getReturnType();
                        if (returnType.equals(boolean.class)) {
                            return Boolean.FALSE;
                        }
                        return new Object();
                    }
                }
            }
        }
        return joinPoint.proceed();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
