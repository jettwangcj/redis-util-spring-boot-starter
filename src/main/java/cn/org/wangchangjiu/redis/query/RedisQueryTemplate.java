package cn.org.wangchangjiu.redis.query;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @Classname RedisQueryTemplate
 * @Description
 * @Date 2023/7/4 18:05
 * @Created by wangchangjiu
 *
 *  *  首先我们可以采用多条件模糊查询章节所说的方式，将我们所涉及到的条件字段作为hash的field，
 *  *          *  而数据的内容则作为对应value进行存储(一般以json格式存储，方便反序列化)。
 *  *          *  hash   100:wang:nan = user json
 *  *          *
 *  *          * 我们需要实现约定好查询的格式，用前面一节的例子来说，field中的命名规则为<id>:<姓名>:<性别>，
 *  *          * 我们每次可以通过"*"来实现我们希望的模糊匹配条件，比如“*：*：男”就是匹配所有男性数据，
 *  *          * “100*：*：*”就是匹配所有id前缀为100的用户。
 *  *          *
 *  *          * 当我们拿到了匹配串后我们先去Redis中寻找是否存在以该匹配串为key的ZSet，
 *  *          * 如果没有则通过Redis提供的HSCAN遍历所有hash的field，
 *  *          * 得到所有符合条件的field，并将其放入一个ZSet集合，同时将这个集合的key设置为我们的条件匹配串。
 *  *          * 如果已经存在了，则直接对这个ZSet进行分页查询即可。对ZSet进行分页的方式已经在前面叙述过了。
 *  *          * 通过这样的方式我们就实现了最简单的分页+多条件模糊查询。
 *
 *
 */
public class RedisQueryTemplate {

    private static final String ZSET_KEY = "%s_%s";

    private StringRedisTemplate stringRedisTemplate;

    private Executor executor;

    public RedisQueryTemplate(StringRedisTemplate stringRedisTemplate, Executor executor){
        this.stringRedisTemplate = stringRedisTemplate;
        this.executor = executor;
    }

    public <T> List<T> query(QueryWrapper queryWrapper){

        FieldRuleKey fieldRuleKey = QueryUtil.checkGetFieldRuleKey(queryWrapper.getEntityClass());

        // 通过 规则 拿到 key值，*:*:nan zset
        String fieldRuleDataKey = Arrays.stream(fieldRuleKey.queryFields()).map(field -> {
            String fieldValue = queryWrapper.getQueryFields().containsKey(field) ?
                    (String) queryWrapper.getQueryFields().get(field) : "*";
            return fieldValue;
        }).collect(Collectors.joining(":"));


        if(fieldRuleDataKey.contains("*")){
            if(stringRedisTemplate.hasKey(fieldRuleDataKey)){

               int start = queryWrapper.getPage() * queryWrapper.getSize();

               // user_*:28:* (.....) zset
               String zsetKey = String.format(ZSET_KEY, fieldRuleKey.keyName(),fieldRuleDataKey);

                Set<String> dataKeys = queryWrapper.isDesc() ? stringRedisTemplate.opsForZSet().reverseRange(zsetKey, start, start + queryWrapper.getSize()):
                        stringRedisTemplate.opsForZSet().range(zsetKey, start, start + queryWrapper.getSize());

                if(CollectionUtils.isEmpty(dataKeys)){
                    // 扫描数据

                   // stringRedisTemplate.opsForHash().scan()

                }


                List<Object> list = stringRedisTemplate.opsForHash().multiGet(fieldRuleKey.keyName(), Collections.singleton(dataKeys));
            } else {



            }
        } else {
            stringRedisTemplate.opsForHash().get(fieldRuleKey.keyName(), fieldRuleDataKey);
        }
        return null;
    }

    public <T> void add(T data, AddParamWrapper addParamWrapper){

        // 获取注解
        FieldRuleKey fieldRuleKey = QueryUtil.checkGetFieldRuleKey(data.getClass());

        // 数据序列化存储
        String dataString = JSON.toJSONString(data);

        // hash 结构 hashKey 由查询字段的数据组成
        String fieldRuleDataKey = Arrays.stream(fieldRuleKey.queryFields()).map(field -> {
            Field f = ReflectionUtils.findField(data.getClass(), field);
            f.setAccessible(true);
            Object value = ReflectionUtils.getField(f, data);
            return value == null ? "" : value.toString();
        }).collect(Collectors.joining(":"));

        // 数据存储在 hash 结构中
        stringRedisTemplate.opsForHash().put(fieldRuleKey.keyName(), fieldRuleDataKey, dataString);

        // 开启新线程 同步写入 zset
        // *:*:nan, xiao:20:nan
        executor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });

    }


    private void syncZsetData(){

    }

}
