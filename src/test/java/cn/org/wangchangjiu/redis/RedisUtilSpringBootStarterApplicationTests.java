package cn.org.wangchangjiu.redis;

import cn.org.wangchangjiu.redis.mq.RedisMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedisUtilSpringBootStarterApplicationTests {

    @Autowired
    RedisMessageProducer redisMessageProducer;

    @Test
    void contextLoads() {

        redisMessageProducer.sendSimpleMessage("hello", null);

    }

}
