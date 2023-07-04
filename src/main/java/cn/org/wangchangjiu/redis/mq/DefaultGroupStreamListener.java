package cn.org.wangchangjiu.redis.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

import java.lang.reflect.InvocationTargetException;

/**
 * @Classname DefaultStreamListener
 * @Description
 * @Date 2023/7/3 21:48
 * @Created by wangchangjiu
 */
@Slf4j
public class DefaultGroupStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    private RedisMessageConsumerContainer consumerContainer;

    public DefaultGroupStreamListener(RedisMessageConsumerContainer consumerContainer){
        this.consumerContainer = consumerContainer;
    }
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            consumerContainer.invoke(message);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
