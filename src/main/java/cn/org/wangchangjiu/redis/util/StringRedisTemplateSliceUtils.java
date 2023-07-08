package cn.org.wangchangjiu.redis.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.util.*;
import java.util.stream.Collectors;

/**
 *  大key 分片处理
 *
 *   其他方法用到再补充
 */
@Slf4j
public class StringRedisTemplateSliceUtils {

    private static final Integer BUCKET_NUMBER = 20;

    private StringRedisTemplate redisTemplate;

    public StringRedisTemplateSliceUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     *  hash 分片删除
     * @param key
     * @param hashKey
     */
    public void hashDelete(String key, String hashKey, Integer bucket){
        if(StringUtils.isEmpty(key) || StringUtils.isEmpty(hashKey)){
            log.error("key is empty or hashKey is empty");
            return;
        }
        key = sliceKeyWrapper(key, hashKey, bucket);
        redisTemplate.opsForHash().delete(key, hashKey);
        log.info("hash delete key:{}, hashKey:{}", key, hashKey);
    }


    /**
     *  大key 分片 hash put
     * @param key
     * @param hashKey
     * @param value
     */
    public void hashPut(String key, String hashKey, String value, Integer bucket){
        if(StringUtils.isEmpty(key) || StringUtils.isEmpty(hashKey) || StringUtils.isEmpty(value)){
            log.error("key is empty or hashKey is empty or value is empty");
            return;
        }
        key = sliceKeyWrapper(key, hashKey, bucket);
        redisTemplate.opsForHash().put(key, hashKey, value);
        log.info("hash put key:{}, hashKey:{}, value:{}", key, hashKey, value);
    }


    /**
     *  大key 分片 hash putAll
     * @param key
     * @param valueMap
     */
    public void hashPutAll(String key, Map<String, String> valueMap, Integer bucket){

        if(StringUtils.isEmpty(key) || CollectionUtils.isEmpty(valueMap)){
            log.error("key is empty or hashKeys is empty");
            return;
        }

        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();

        redisTemplate.executePipelined((RedisCallback<Boolean>) connection -> {
            Iterator<Map.Entry<String, String>> iterator = valueMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                String tempKey = sliceKeyWrapper(key, entry.getKey(), bucket);
                byte[] rawKey = keySerializer.serialize(tempKey);
                byte[] rawHashKey = hashKeySerializer.serialize(entry.getKey());
                byte[] rawHashValue = valueSerializer.serialize(entry.getValue());
                connection.hashCommands().hSet(rawKey, rawHashKey, rawHashValue);
                log.info("hash hSet key:{}, hashKey:{}, value:{}", tempKey, entry.getKey(), entry.getValue());
            }
            return null;
        });
    }


    /**
     *  大key 分片 hash get
     * @param key
     * @param hashKey
     * @return
     */
    public String hashGet(String key, String hashKey, Integer bucket){
        if(StringUtils.isEmpty(key) || StringUtils.isEmpty(hashKey)){
            log.error("key is empty or hashKey is empty");
            return null;
        }

        key = sliceKeyWrapper(key, hashKey, bucket);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String value = hashOperations.get(key, hashKey);
        log.info("hash get key:{}, hashKey:{}, value:{}", key, hashKey, value);
        return value;
    }

    /**
     *  大key 分片 hash 批量GET
     * @param key
     * @param hashKeys
     * @return
     */
    public List<String> hashMultiGet(String key, Collection<String> hashKeys, Integer bucket){

        if(StringUtils.isEmpty(key) || CollectionUtils.isEmpty(hashKeys)){
            log.error("key is empty or hashKeys is empty");
            return new ArrayList<>();
        }

        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
        List<Object> values = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                hashKeys.stream().forEach(hashKey -> {
                    String tempKey = sliceKeyWrapper(key, hashKey, bucket);
                    byte[] rawKey = keySerializer.serialize(tempKey);
                    byte[] rawHashKey = hashKeySerializer.serialize(hashKey);
                    connection.hashCommands().hGet(rawKey, rawHashKey);
                });
                return null;
            }
        }, valueSerializer);

        if(!CollectionUtils.isEmpty(values)){
            return values.stream().map(item -> String.class.cast(item)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    public List<String> hashValues(String keyPrefix, Integer bucket){
        List<String> values = new ArrayList<>();
        Set<String> keys = this.scanKeys(keyPrefix, BUCKET_NUMBER);

        if(!CollectionUtils.isEmpty(keys)){
            keys.stream().forEach(currKey -> {
                Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(currKey, ScanOptions.NONE);
                while (cursor.hasNext()) {
                    Map.Entry<Object, Object> entry = cursor.next();
                    values.add(String.valueOf(entry.getValue()));
                }
            });
        }
        return values;
    }



    /**
     *  扫描 Key
     * @param keyPrefix
     * @param pageSize
     * @return
     */
    public Set<String> scanKeys(String keyPrefix, long pageSize) {
        try {
            Set<String> binaryKeys = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match(keyPrefix).count(pageSize).build();
            RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
            log.info(options.toOptionString());

            Cursor<String> cursor= (Cursor) redisTemplate.executeWithStickyConnection(
                    redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
            while (cursor.hasNext()) {
                binaryKeys.add(cursor.next());
            }
            log.info("分页scan keys:{}, {}个", keyPrefix, binaryKeys.size());
            return binaryKeys;
        } catch (Exception e) {
            log.error("获取redis keys异常", e);
        }
        return Collections.emptySet();
    }


    /**
     *  分片 key
     * @param key
     * @param hashKey
     * @return
     */
    private String sliceKeyWrapper(String key, String hashKey, Integer bucket){
        bucket = bucket == null ? BUCKET_NUMBER : bucket;
        return String.format(key, Math.abs(hashKey.hashCode() % bucket));
    }


}
