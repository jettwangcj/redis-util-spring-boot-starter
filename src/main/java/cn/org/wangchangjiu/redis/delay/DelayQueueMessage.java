package cn.org.wangchangjiu.redis.delay;

import java.util.concurrent.TimeUnit;

/**
 * @Classname DelayQueueMessage
 * @Description
 * @Date 2022/9/14 10:15
 * @Created by wangchangjiu
 */
public interface DelayQueueMessage {

    <T> void addDelayQueue(T value, long delay, TimeUnit timeUnit, String queueCode);

    <T> T getDelayQueue(String queueCode);

}
