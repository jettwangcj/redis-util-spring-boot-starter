package cn.org.wangchangjiu.redis.web.limit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class RedisRateLimiter {

    private static final String EMPTY_KEY = "____EMPTY_KEY__";

    private RedisTemplate<String, Object> redisTemplate;
    private RedisScript<Number> script;

    public RedisRateLimiter(RedisTemplate<String, Object> redisTemplate, RedisScript<Number> script){

        this.redisTemplate = redisTemplate;
        this.script = script;
    }

    private List<String> getKeys(String id) {
        String prefix = "request_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    public boolean isAllowed(String id, int replenishRate, int burstCapacity) {
        if(EMPTY_KEY.equalsIgnoreCase(id)){
            // 忽略 EMPTY_KEY
            return true;
        }

        try {
            List<String> keys = getKeys(id);
           // List<String> scriptArgs = Arrays.asList();
            Number result  = this.redisTemplate.execute(this.script, keys, replenishRate, burstCapacity, Instant.now().getEpochSecond(), 1);
            return result.intValue() == 1;
        } catch (Exception var9) {
            log.error("Error determining if user allowed from redis", var9);
            return false;
        }
    }

}
