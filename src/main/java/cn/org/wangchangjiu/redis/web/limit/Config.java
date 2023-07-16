package cn.org.wangchangjiu.redis.web.limit;

import lombok.Data;

@Data
public class Config<T> {

    protected T mathKey;

    /**
     * 令牌桶美秒填充速率
     */
    protected int replenishRate;

    /**
     * 令牌桶容量
     */
    protected int burstCapacity;

    /**
     *
     */
    protected KeyResolver keyResolver;

    public static class RequestConfig extends Config<String> {

        public RequestConfig(String path, int replenishRate, int burstCapacity, KeyResolver keyResolver) {
            this.mathKey = path;
            this.burstCapacity = burstCapacity;
            this.replenishRate = replenishRate;
            this.keyResolver = keyResolver;
        }
    }

    public static class AopConfig extends Config<Class<? extends KeyResolver>> {

        public AopConfig(Class<? extends KeyResolver> mathKey, int replenishRate, int burstCapacity, KeyResolver keyResolver) {
            this.mathKey = mathKey;
            this.burstCapacity = burstCapacity;
            this.replenishRate = replenishRate;
            this.keyResolver = keyResolver;
        }
    }

}
