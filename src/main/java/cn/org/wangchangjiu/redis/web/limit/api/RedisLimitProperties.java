package cn.org.wangchangjiu.redis.web.limit.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "redis.util.limit")
public class RedisLimitProperties {

    /**
     *  匹配多个限流配置
     */
    private List<Config> configs = new ArrayList<>();

    /**
     *  限流配置
     */
    @Data
    public static class Config {

        /**
         *  接口 path
         */
        private String path;

        /**
         * 令牌桶美秒填充速率
         */
        private int replenishRate;

        /**
         * 令牌桶容量
         */
        private int burstCapacity;

        /**
         *
         */
        private String keyResolver;
    }


}
