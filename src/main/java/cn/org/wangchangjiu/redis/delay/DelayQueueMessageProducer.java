package cn.org.wangchangjiu.redis.delay;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Classname DelayQueueMessageProducer
 * @Description 延迟队列生产者
 * @Date 2022/9/14 10:12
 * @Created by wangchangjiu
 */
@Slf4j
public class DelayQueueMessageProducer {

    private DelayQueueMessage delayQueueMessage;

    private String registerService;

    public DelayQueueMessageProducer(DelayQueueMessage delayQueueMessage, String registerService){
        this.delayQueueMessage = delayQueueMessage;
        this.registerService = registerService;
    }

    public <T> void sendMessage(String topic, T jsonSerializableObject, long delay, TimeUnit timeUnit) {
        RedisDelayMessage message = new RedisDelayMessage(topic, jsonSerializableObject);
        delayQueueMessage.addDelayQueue(message, delay, timeUnit, String.format(Constant.QUEUE_CODE, registerService));
        log.info("添加消息：{} 进入 topic ：{} 延迟队列，delay:{} ,timeUnit:{}", JSON.toJSONString(message), topic, delay, timeUnit);
    }
}
