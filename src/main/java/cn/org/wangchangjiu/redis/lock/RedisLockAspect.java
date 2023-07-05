package cn.org.wangchangjiu.redis.lock;

import cn.org.wangchangjiu.redis.common.MyAopUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * <b> 分布式锁实现 </b>
 * <br/>
 * <b>对使用@RedisLock的方法生效</b>
 *
 * @author Tom Lee
 */
@Aspect
@Order(1000)
@Slf4j
public class RedisLockAspect {

    private RedissonClient redissonClient;

    public RedisLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(cn.org.wangchangjiu.redis.lock.RedisLock)")
    public Object lock(ProceedingJoinPoint pjp) throws Throwable {
        RedisLock redisLock = MyAopUtil.getMethodAnnotation(pjp, RedisLock.class);
        if(redisLock != null){
            Method method = MyAopUtil.getCurrentMethod(pjp);
            String key = SpElUtil.parseSpEl(method, pjp.getArgs(), redisLock.synKey());

            String lockKey = String.format("redisson_lock:%s", key);
            RLock lock = redissonClient.getLock(lockKey);
            try {
                if (lock.tryLock(redisLock.waitMills(), redisLock.keepMills(), redisLock.timeUnit())) {
                    return pjp.proceed();
                }
            } catch (Throwable throwable) {
                throw new RedissonLockException(throwable.getMessage());
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

            if(redisLock.exceptionWithoutLock()) {
                throw new RedissonLockException("get redisson lock fail");
            } else {
                Signature signature = pjp.getSignature();
                Class returnType = ((MethodSignature) signature).getReturnType();
                if (returnType.equals(boolean.class)) {
                    return Boolean.FALSE;
                }
                return new Object();
            }
        }
        return pjp.proceed();
    }

}
