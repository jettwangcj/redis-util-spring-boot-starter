package cn.org.wangchangjiu.redis.query;

import com.alibaba.fastjson.JSON;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @Classname RedisQueryTemplate
 * @Description
 * @Date 2023/7/4 18:05
 * @Created by wangchangjiu
 * <p>
 * *  首先我们可以采用多条件模糊查询章节所说的方式，将我们所涉及到的条件字段作为hash的field，
 * *          *  而数据的内容则作为对应value进行存储(一般以json格式存储，方便反序列化)。
 * *          *  hash   100:wang:nan = user json
 * *          *
 * *          * 我们需要实现约定好查询的格式，用前面一节的例子来说，field中的命名规则为<id>:<姓名>:<性别>，
 * *          * 我们每次可以通过"*"来实现我们希望的模糊匹配条件，比如“*：*：男”就是匹配所有男性数据，
 * *          * “100*：*：*”就是匹配所有id前缀为100的用户。
 * *          *
 * *          * 当我们拿到了匹配串后我们先去Redis中寻找是否存在以该匹配串为key的ZSet，
 * *          * 如果没有则通过Redis提供的HSCAN遍历所有hash的field，
 * *          * 得到所有符合条件的field，并将其放入一个ZSet集合，同时将这个集合的key设置为我们的条件匹配串。
 * *          * 如果已经存在了，则直接对这个ZSet进行分页查询即可。对ZSet进行分页的方式已经在前面叙述过了。
 * *          * 通过这样的方式我们就实现了最简单的分页+多条件模糊查询。
 */
public class RedisQueryTemplate {

    private StringRedisTemplate stringRedisTemplate;

    private Executor executor;

    public RedisQueryTemplate(StringRedisTemplate stringRedisTemplate, Executor executor) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.executor = executor;
    }

    public <T> List<T> query(QueryWrapper queryWrapper, Class<T> entityClass) throws InvocationTargetException, IllegalAccessException {

        FieldRuleKey fieldRuleKey = QueryUtil.checkGetFieldRuleKey(entityClass);

        // 通过 规则 拿到 key值，*:*:nan zset
        String fieldRuleDataKey = Arrays.stream(fieldRuleKey.queryFields()).map(field -> {
            String fieldValue = queryWrapper.getQueryFields().containsKey(field) ? queryWrapper.getQueryFields().get(field) : "*";
            return fieldValue;
        }).collect(Collectors.joining(":"));


        List<T> data = new ArrayList<>();
        if (fieldRuleDataKey.contains("*")) {
            if (stringRedisTemplate.hasKey(fieldRuleDataKey)) {

                int start = queryWrapper.getPage() * queryWrapper.getSize();

                // user_*:28:* (.....) zset
                String zsetKey = String.format(QueryCommon.ZSET_KEY, fieldRuleKey.keyName(), fieldRuleDataKey);

                Set<String> dataKeys = queryWrapper.isDesc() ? stringRedisTemplate.opsForZSet().reverseRange(zsetKey, start, start + queryWrapper.getSize()) :
                        stringRedisTemplate.opsForZSet().range(zsetKey, start, start + queryWrapper.getSize());

                // zset 中没有找到 数据
                if (CollectionUtils.isEmpty(dataKeys)) {
                    data.addAll(scanHashKey(entityClass, fieldRuleDataKey));
                } else {
                    List<Object> list = stringRedisTemplate.opsForHash().multiGet(fieldRuleKey.keyName(), Collections.singleton(dataKeys));
                    List<T> collect = list.stream().map(item -> JSON.parseObject(String.valueOf(item), entityClass)).collect(Collectors.toList());
                    data.addAll(collect);
                }
            } else {
                // 扫描 key
                data.addAll(scanHashKey(entityClass, fieldRuleDataKey));
            }
        } else {
            Object dataObj = stringRedisTemplate.opsForHash().get(fieldRuleKey.keyName(), fieldRuleDataKey);
            data.add(JSON.parseObject(String.valueOf(dataObj), entityClass));
        }
        return data;
    }

    /**
     *  扫描key 写入缓存
     * @param entityClass
     * @param fieldRuleDataKey
     * @return
     * @param <T>
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private <T> List<T> scanHashKey(Class<T> entityClass,  String fieldRuleDataKey) throws InvocationTargetException, IllegalAccessException {
        List<T> data = new ArrayList<>();
        FieldRuleKey fieldRuleKey = QueryUtil.checkGetFieldRuleKey(entityClass);
        // 扫描数据
        Cursor<Map.Entry<Object, Object>> cursor = stringRedisTemplate.opsForHash().scan(fieldRuleKey.keyName(), ScanOptions.scanOptions().match(fieldRuleDataKey).build());
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
        String scoreMethod = fieldRuleKey.getScoreMethod();
        Method method = ReflectionUtils.findMethod(entityClass, scoreMethod);

        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor.next();
            T item = JSON.parseObject(String.valueOf(entry.getValue()), entityClass);
            Double invokeValue = (Double) method.invoke(item);
            tuples.add(ZSetOperations.TypedTuple.of(String.valueOf(entry.getValue()), invokeValue));
            data.add(item);
        }

        // 数据写回 zset
        String zsetKey = String.format(QueryCommon.ZSET_KEY, fieldRuleKey.keyName(), fieldRuleDataKey);
        if(stringRedisTemplate.hasKey(zsetKey)){
            stringRedisTemplate.opsForZSet().add(zsetKey, tuples);
        } else {
            stringRedisTemplate.opsForZSet().add(zsetKey, tuples);
            stringRedisTemplate.expire(zsetKey, fieldRuleKey.expire(), fieldRuleKey.timeUnit());
        }
        return data;
    }

    public <T> void add(T data) {

        // 获取注解
        FieldRuleKey fieldRuleKey = QueryUtil.checkGetFieldRuleKey(data.getClass());

        // 数据序列化存储
        String dataString = JSON.toJSONString(data);

        // hash 结构 hashKey 由查询字段的数据组成 xiaohei:27:0
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
        executor.execute(() -> {
            try {
                syncZsetData(data, fieldRuleKey, fieldRuleDataKey);
            } catch (IllegalAccessException | InvocationTargetException e) {

            }
        });

    }

    /**
     *  同步 zset 索引数据
     * @param data
     * @param fieldRuleKey
     * @param fieldRuleDataKey
     * @param <T>
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private <T> void syncZsetData(T data, FieldRuleKey fieldRuleKey, String fieldRuleDataKey) throws IllegalAccessException, InvocationTargetException {
        Method method = ReflectionUtils.findMethod(data.getClass(), fieldRuleKey.getScoreMethod());
        Double score = (Double) method.invoke(data);

        // xiaohei:28:1
        // * : 28 : 1

        String prefix = fieldRuleKey.keyName() + "_";
        Cursor<String> scan = stringRedisTemplate.scan(ScanOptions.scanOptions().match(prefix).build());
        while (scan.hasNext()) {

            // user_xiaohei:*:*
            String next = scan.next();

            // xiaohei:*:*
            String ruleKey = next.split("_")[1];

            // xiaohei:271:0 匹配 xiaohei:27*:*
            List<String> generateAllKey = QueryCommon.generateAllKey(fieldRuleKey.keyName(), ruleKey);

            stringRedisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    generateAllKey.stream().forEach(key -> {
                        if(!operations.hasKey(key)) {
                            operations.opsForZSet().add(key, fieldRuleDataKey, score);
                            operations.expire(key, fieldRuleKey.expire(), fieldRuleKey.timeUnit());
                        } else {
                            operations.opsForZSet().add(key, fieldRuleDataKey, score);
                        }
                    });
                    return null;
                }
            });
        }
    }
}
