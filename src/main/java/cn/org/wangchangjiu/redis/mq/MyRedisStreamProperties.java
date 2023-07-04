package cn.org.wangchangjiu.redis.mq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @Classname MyRedisStreamProperties
 * @Description
 * @Date 2023/7/4 14:37
 * @Created by wangchangjiu
 */
@ConfigurationProperties(
        prefix = "redis.util.mq"
)
@Data
public class MyRedisStreamProperties {

    private Options options = new Options();


    @Data
    class Options {

        private Duration pollTimeout = Duration.ofSeconds(2);
        private Integer batchSize = 3;

    }

}
