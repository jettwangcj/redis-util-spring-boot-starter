package cn.org.wangchangjiu.redis.delay;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Classname DelayQueueMessageConsumer
 * @Description 延迟队列消费者
 * @Date 2022/9/14 10:28
 * @Created by wangchangjiu
 */
@Slf4j
public class DelayQueueMessageConsumer implements BeanPostProcessor {

    private final Map<String, RedisDelayMessageConsumerContainer> consumerContainerGroups = new HashMap<>();

    private DelayQueueMessage delayQueueMessage;


    private String registerService;

    public DelayQueueMessageConsumer(DelayQueueMessage delayQueueMessage,
                                     String registerService){
        this.delayQueueMessage = delayQueueMessage;
        this.registerService = registerService;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Map<Method, RedisDelayMessageListener> temp = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<RedisDelayMessageListener>) method -> AnnotationUtils.findAnnotation(method, RedisDelayMessageListener.class));
        if (!CollectionUtils.isEmpty(temp)) {
            for (Map.Entry<Method, RedisDelayMessageListener> entry : temp.entrySet()) {
                RedisDelayMessageConsumerContainer consumerContainer = new RedisDelayMessageConsumerContainer(bean, entry.getKey(), entry.getValue());
                RedisDelayMessageListener redisDelayMessageListener = entry.getValue();
                consumerContainerGroups.merge(redisDelayMessageListener.queue(),
                        consumerContainer,
                        (redisDelayMessageConsumerContainer, redisDelayMessageConsumerContainer2) -> redisDelayMessageConsumerContainer2);

            }
        }
        return bean;
    }


    @PostConstruct
    public void consumerAndSendKafka() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            String QUEUE_CODE = String.format(Constant.QUEUE_CODE, registerService);
            while (true) {
                log.info("(Redis延迟队列消费中...)");
                try{
                    Object obj = delayQueueMessage.getDelayQueue(QUEUE_CODE);
                    if(obj != null){
                        RedisDelayMessage message = RedisDelayMessage.class.cast(obj);

                        RedisDelayMessageConsumerContainer consumerContainer = consumerContainerGroups.get(message.getTopic());
                        if(consumerContainer == null){
                            log.error("topic :{} consumer not found", message.getTopic());
                        } else {
                            consumerContainer.invoke(message.getValue());
                            log.info("--延迟队列中间件...topic:{}, 获取延迟消息：{}, 发送执行业务--", message.getTopic(), message.getValue());
                        }
                    }
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
                try{
                    Thread.sleep(500);
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        });
        log.info("(Redis延迟队列启动成功)");
    }
}
