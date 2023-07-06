package cn.org.wangchangjiu.redis.delay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Classname RedissonDelayProperties
 * @Description
 * @Date 2022/9/15 9:51
 * @Created by wangchangjiu
 */
@Data
@ConfigurationProperties(prefix = "redis.util.delay")
public class RedissonDelayProperties {

    private String registerService = "other";

}
