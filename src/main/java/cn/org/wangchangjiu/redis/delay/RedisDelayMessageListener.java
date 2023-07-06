package cn.org.wangchangjiu.redis.delay;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname RedisMessageListener
 * @Description
 * @Date 2023/7/3 21:00
 * @Created by wangchangjiu
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisDelayMessageListener {

    String topic() default "default";

}
