package cn.org.wangchangjiu.redis.mq;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Classname RedisMessageConsumerContainer
 * @Description
 * @Date 2023/7/3 21:14
 * @Created by wangchangjiu
 */
public class RedisMessageConsumerContainer {

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
    private RedisMessageListener redisMessageListener;

    public RedisMessageConsumerContainer(Object bean, Method method, RedisMessageListener redisMessageListener) {
        this.bean = bean;
        this.method = method;
        this.redisMessageListener = redisMessageListener;

        if(!StringUtils.hasText(this.redisMessageListener.groupId()) || !StringUtils.hasText(this.redisMessageListener.queue()) ){
            throw new RuntimeException("redisMessageListener groupId or queue cannot empty");
        }

        if(this.redisMessageListener.groupId().contains("#") || this.redisMessageListener.queue().contains("#")){
            throw new RuntimeException("redisMessageListener groupId or queue cannot contains '#' ");
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if(parameterTypes.length != 1){
            throw new RuntimeException(String.format("RedisMessageConsumerContainer:Method[%s] parameter number error", method.getName()));
        }
        String paramTypeRecord = "org.springframework.data.redis.connection.stream.MapRecord";
        if(!paramTypeRecord.equals(parameterTypes[0].getName())){
            throw new RuntimeException(String.format("RedisMessageConsumerContainer:Method[%s] parameter type error,need %s but find %s",
                    method.getName(), paramTypeRecord,parameterTypes[0].getName()));
        }
    }


    public void invoke(MapRecord<String, String, String> consumerRecord) throws InvocationTargetException, IllegalAccessException {
        method.invoke(bean, consumerRecord);
    }


    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public RedisMessageListener getRedisMessageListener() {
        return redisMessageListener;
    }
}
