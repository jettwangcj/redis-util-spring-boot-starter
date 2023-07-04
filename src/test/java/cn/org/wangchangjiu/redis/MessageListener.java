package cn.org.wangchangjiu.redis;

import cn.org.wangchangjiu.redis.mq.RedisMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

/**
 * @Classname MessageListener
 * @Description TODO
 * @Date 2023/7/4 15:27
 * @Created by wangchangjiu
 */
@Slf4j
@Component
public class MessageListener {

    @RedisMessageListener(queue = "hello", groupId = "hello-a")
    public void listener(MapRecord<String, String, String> message)  {
        log.info("message lis:{}", message);
        message.getId();

    }


}
