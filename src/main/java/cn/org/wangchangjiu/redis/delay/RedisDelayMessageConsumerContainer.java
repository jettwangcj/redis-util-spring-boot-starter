package cn.org.wangchangjiu.redis.delay;

import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Classname RedisMessageConsumerContainer
 * @Description
 * @Date 2023/7/3 21:14
 * @Created by wangchangjiu
 */
public class RedisDelayMessageConsumerContainer {

    /**
     * 消费逻辑方法所属bean
     */
    private Object bean;

    /**
     * 业务消费方法
     */
    private Method method;

    /**
     * 注解详情
     */
    private RedisDelayMessageListener redisDelayMessageListener;

    public RedisDelayMessageConsumerContainer(Object bean, Method method, RedisDelayMessageListener redisDelayMessageListener) {
        this.bean = bean;
        this.method = method;
        this.redisDelayMessageListener = redisDelayMessageListener;

        if(!StringUtils.hasText(this.redisDelayMessageListener.topic())){
            throw new RuntimeException("redisMessageListener groupId or queue cannot empty");
        }

    }


    public <T> void invoke(T message) throws InvocationTargetException, IllegalAccessException {
        method.invoke(bean, message);
    }

}
