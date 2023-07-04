package cn.org.wangchangjiu.redis.mq;

import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;

/**
 * @Classname RedisMessageProducer
 * @Description
 * @Date 2023/7/3 20:36
 * @Created by wangchangjiu
 */
public class RedisMessageProducer {


    private StringRedisTemplate stringRedisTemplate;

    public RedisMessageProducer(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public <T> void sendSimpleMessage(String queue, T jsonSerializableObject) {

        // 创建消息记录, 以及指定stream
        StringRecord stringRecord = StreamRecords.string(Collections.singletonMap("name", "test")).withStreamKey(queue);
        // 将消息添加至消息队列中
        this.stringRedisTemplate.opsForStream().add(stringRecord);

    }

}
