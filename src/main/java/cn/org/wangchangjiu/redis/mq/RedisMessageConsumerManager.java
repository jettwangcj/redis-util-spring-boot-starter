package cn.org.wangchangjiu.redis.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname RedisMessageConsumerManager
 * @Description redis 消息 消费者管理
 * @Date 2023/7/3 21:04
 * @Created by wangchangjiu
 */
@Slf4j
public class RedisMessageConsumerManager implements BeanPostProcessor {

    private final Map<String, RedisMessageConsumerContainer> consumerContainerGroups = new HashMap<>();


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Map<Method, RedisMessageListener> temp = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<RedisMessageListener>) method -> AnnotationUtils.findAnnotation(method, RedisMessageListener.class));
        if (!CollectionUtils.isEmpty(temp)) {
            for (Map.Entry<Method, RedisMessageListener> entry : temp.entrySet()) {
                RedisMessageConsumerContainer consumerContainer = new RedisMessageConsumerContainer(bean, entry.getKey(), entry.getValue());
                RedisMessageListener redisMessageListener = entry.getValue();
                consumerContainerGroups.merge(redisMessageListener.groupId() + "#" + redisMessageListener.queue(),
                        consumerContainer,
                        (redisMessageConsumerContainer, redisMessageConsumerContainer2) -> redisMessageConsumerContainer2);

            }
        }
        return bean;
    }

    public Map<String, RedisMessageConsumerContainer> getConsumerContainerGroups() {
        return consumerContainerGroups;
    }

}
