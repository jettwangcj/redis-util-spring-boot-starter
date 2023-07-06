package cn.org.wangchangjiu.redis;

import cn.org.wangchangjiu.redis.delay.DelayQueueAutoConfiguration;
import cn.org.wangchangjiu.redis.lock.RedisLockAutoConfiguration;
import cn.org.wangchangjiu.redis.mq.RedisStreamAutoConfiguration;
import cn.org.wangchangjiu.redis.web.duplicate.DuplicateSubmitAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @Classname RedisUtilAutoConfiguration
 * @Description
 * @Date 2023/7/6 18:18
 * @Created by wangchangjiu
 */
@AutoConfiguration
@Import({ DelayQueueAutoConfiguration.class,
          RedisLockAutoConfiguration.class,
          RedisStreamAutoConfiguration.class,
          DuplicateSubmitAutoConfiguration.class
  })
public class RedisUtilAutoConfiguration {
}
