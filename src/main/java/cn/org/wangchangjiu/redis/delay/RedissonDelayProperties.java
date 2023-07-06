package cn.org.wangchangjiu.redis.delay;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
