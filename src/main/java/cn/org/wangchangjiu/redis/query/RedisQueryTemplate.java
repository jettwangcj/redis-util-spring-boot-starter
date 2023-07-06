package cn.org.wangchangjiu.redis.query;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @Classname RedisQueryTemplate
 * @Description
 * @Date 2023/7/4 18:05
 * @Created by wangchangjiu
 */
public class RedisQueryTemplate {

    private StringRedisTemplate stringRedisTemplate;

    public <T> List<T> query(QueryWrapper queryWrapper){

        queryWrapper.getQueryFields().forEach((fieldName, paramValue) -> {

        });


        /**
         *  首先我们可以采用多条件模糊查询章节所说的方式，将我们所涉及到的条件字段作为hash的field，
         *  而数据的内容则作为对应value进行存储(一般以json格式存储，方便反序列化)。
         *   100:wang:nan = user json  hash
         *
         * 我们需要实现约定好查询的格式，用前面一节的例子来说，field中的命名规则为<id>:<姓名>:<性别>，
         * 我们每次可以通过"*"来实现我们希望的模糊匹配条件，比如“*：*：男”就是匹配所有男性数据，
         * “100*：*：*”就是匹配所有id前缀为100的用户。
         *
         * 当我们拿到了匹配串后我们先去Redis中寻找是否存在以该匹配串为key的ZSet，
         * 如果没有则通过Redis提供的HSCAN遍历所有hash的field，
         * 得到所有符合条件的field，并将其放入一个ZSet集合，同时将这个集合的key设置为我们的条件匹配串。
         * 如果已经存在了，则直接对这个ZSet进行分页查询即可。对ZSet进行分页的方式已经在前面叙述过了。
         * 通过这样的方式我们就实现了最简单的分页+多条件模糊查询。
         */



        return null;
    }

    public <T> void add(String key, T data, AddParamWrapper addParamWrapper){

        stringRedisTemplate.opsForHash();


    }

}
