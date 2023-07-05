package cn.org.wangchangjiu.redis.query;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * @Classname RedisQueryTemplate
 * @Description
 * @Date 2023/7/4 18:05
 * @Created by wangchangjiu
 */
public class RedisQueryTemplate {

    private RedisTemplate<Object, Object> redisTemplate;

    public <T> List<T> query(QueryWrapper queryWrapper){

        return null;
    }

    public <T> void add(T data, QueryWrapper queryWrapper){



    }

}
