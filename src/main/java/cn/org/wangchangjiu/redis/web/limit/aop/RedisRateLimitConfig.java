package cn.org.wangchangjiu.redis.web.limit.aop;

import cn.org.wangchangjiu.redis.web.limit.KeyResolver;

public @interface RedisRateLimitConfig {

    /**
     *  令牌每次填充数量
     * @return
     */
    int replenishRate() default 1 ;

    /**
     * 令牌桶容量
     */
    int burstCapacity() default 2;


    /**
     * 限流维度（限流Key）
     */
    Class<? extends KeyResolver> keyResolver() default KeyResolver.DefaultKeyResolver.class;


    /**
     *  被限流时是否抛出遗产
     * @return
     */
    boolean exceptionWithLimit() default true;
}
