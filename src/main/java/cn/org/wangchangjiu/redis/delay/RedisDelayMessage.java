package cn.org.wangchangjiu.redis.delay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname RedisDelayMssage
 * @Description
 * @Date 2022/10/14 16:30
 * @Created by wangchangjiu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisDelayMessage<T> implements Serializable {

    private String topic;

    private T value;

}
