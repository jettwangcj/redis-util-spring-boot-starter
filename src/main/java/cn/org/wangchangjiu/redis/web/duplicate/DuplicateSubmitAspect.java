package cn.org.wangchangjiu.redis.web.duplicate;

import cn.org.wangchangjiu.redis.common.MyAopUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Classname DuplicateSubmitAspect
 * @Description 防止重复提交小工具
 * @Date 2022/10/24 11:21
 * @Created by wangchangjiu
 */
@Aspect
@Slf4j
public class DuplicateSubmitAspect {

    private RedisTemplate<Object, Object> redisTemplate;


    DuplicateSubmitAspect(RedisTemplate<Object, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Pointcut("@annotation(cn.org.wangchangjiu.redis.web.duplicate.DuplicateSubmit)")
    public void pointCut() {
    }

    private static Logger logger = LoggerFactory.getLogger(DuplicateSubmitAspect.class);

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method currentMethod = MyAopUtil.getCurrentMethod(joinPoint);
        String duplicateKey = null;
        if(currentMethod != null){
            DuplicateSubmit annotation = currentMethod.getAnnotation(DuplicateSubmit.class);
            if(annotation != null){
                StringBuilder invocationKey = new StringBuilder();
                invocationKey.append(joinPoint.getTarget().getClass().getName()).append("#")
                        .append(joinPoint.getSignature().getName()).append("#")
                        .append(Arrays.toString(joinPoint.getArgs()));
                duplicateKey = "dup:" + DigestUtils.md5DigestAsHex(invocationKey.toString().getBytes(Charset.forName("UTF-8")));

                Boolean lock = redisTemplate.opsForValue().setIfAbsent(duplicateKey, "DUP", annotation.interval(), TimeUnit.SECONDS);
                if(lock == null || !lock){
                    // 未获取锁，重复请求
                    logger.info("检测到重复请求：{}, duplicateKey:{}", invocationKey, duplicateKey);
                    throw new RuntimeException("Please do not resubmit");
                }
            }
        }
        Object object;
        try {
            object = joinPoint.proceed();
        } finally {
            if(duplicateKey != null){
                redisTemplate.delete(duplicateKey);
            }
        }

        return object;
    }



}
