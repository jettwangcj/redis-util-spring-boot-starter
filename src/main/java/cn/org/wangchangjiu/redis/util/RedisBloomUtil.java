package cn.org.wangchangjiu.redis.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisBloom
 * @Description redis 布隆过滤器
 * @Date 2022/10/19 10:22
 * @Created by wangchangjiu
 */

@Slf4j
public class RedisBloomUtil {

    private RedisTemplate<String, Object> redisTemplate;

    public RedisBloomUtil(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    private static RedisScript<Boolean> bfReserveScript = new DefaultRedisScript<>("return redis.call('bf.reserve', KEYS[1], ARGV[1], ARGV[2])", Boolean.class);

    private static RedisScript<Boolean> bfAddScript = new DefaultRedisScript<>("return redis.call('bf.add', KEYS[1], ARGV[1])", Boolean.class);

    private static RedisScript<Boolean> bfExistsScript = new DefaultRedisScript<>("return redis.call('bf.exists', KEYS[1], ARGV[1])", Boolean.class);

    private static String BFMADD_SCRIPT = "return redis.call('bf.madd', KEYS[1], %s)";

    private static String BFMEXISTS_SCRIPT = "return redis.call('bf.mexists', KEYS[1], %s)";

    /**
     * 设置错误率和大小（需要在添加元素前调用，若已存在元素，则会报错）
     * 错误率越低，需要的空间越大
     * @param key
     * @param errorRate 错误率，默认0.01
     * @param initialSize 默认100，预计放入的元素数量，当实际数量超出这个数值时，误判率会上升，尽量估计一个准确数值再加上一定的冗余空间
     * @return
     */
    public Boolean bfReserve(String key, double errorRate, int initialSize){
        return redisTemplate.execute(bfReserveScript, Arrays.asList(key), String.valueOf(errorRate), String.valueOf(initialSize));
    }

    /**
     * 添加元素
     * @param key
     * @param value
     * @return true表示添加成功，false表示添加失败（存在时会返回false）
     */
    public Boolean bfAdd(String key, String value, long timeout){
        Boolean hasKey = redisTemplate.hasKey(key);
        Boolean success = redisTemplate.execute(bfAddScript, Arrays.asList(key), value);
        if(!hasKey){
            redisTemplate.expire(key, timeout, TimeUnit.HOURS);
        }
        return success;
    }

    /**
     * 查看元素是否存在（判断为存在时有可能是误判，不存在是一定不存在）
     * @param key
     * @param value
     * @return true表示存在，false表示不存在
     */
    public Boolean bfExists(String key, String value){
        return redisTemplate.execute(bfExistsScript, Arrays.asList(key), value);
    }

    /**
     * 批量添加元素
     * @param key
     * @param values
     * @return 按序 1表示添加成功，0表示添加失败
     */
    public List<Integer> bfMAdd(String key, String... values){
        return (List<Integer>)redisTemplate.execute(this.generateScript(BFMADD_SCRIPT, values), Arrays.asList(key), values);
    }

    /**
     * 批量检查元素是否存在（判断为存在时有可能是误判，不存在是一定不存在）
     * @param key
     * @param values
     * @return 按序 1表示存在，0表示不存在
     */
    public List<Integer> bfMExists(String key, String... values){
        return (List<Integer>)redisTemplate.execute(this.generateScript(BFMEXISTS_SCRIPT, values), Arrays.asList(key), values);
    }

    private RedisScript<List> generateScript(String script, String[] values) {
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= values.length; i ++){
            if(i != 1){
                sb.append(",");
            }
            sb.append("ARGV[").append(i).append("]");
        }
        return new DefaultRedisScript<>(String.format(script, sb.toString()), List.class);
    }

}
