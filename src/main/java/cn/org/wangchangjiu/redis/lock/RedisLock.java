package cn.org.wangchangjiu.redis.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisLock
 * @Description 分布式锁
 * @Date 2023/7/4 18:29
 * @Created by wangchangjiu
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RedisLock {
    /**
     *  SpEl key
     */
    String synKey();

    /**
     * 锁时间
     */
    long keepMills() default 5 * 1000;

    /**
     *  没有获取到锁 是否抛出异常
     * @return
     */
    boolean exceptionWithoutLock() default true;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 等待锁时间
     *
     * @return return
     */
    long waitMills() default 500;
}
