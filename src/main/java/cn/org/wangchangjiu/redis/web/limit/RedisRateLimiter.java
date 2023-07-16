package cn.org.wangchangjiu.redis.web.limit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class RedisRateLimiter {

    private static final String EMPTY_KEY = "____EMPTY_KEY__";

    private StringRedisTemplate redisTemplate;
    private RedisScript<Long> script;

    public RedisRateLimiter(StringRedisTemplate redisTemplate, RedisScript<Long> script){

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
            List<String> scriptArgs = Arrays.asList(replenishRate + "", burstCapacity + "", Instant.now().getEpochSecond() + "", "1");
            Long result  = this.redisTemplate.execute(this.script, keys, scriptArgs);
            return result == 1L;
        } catch (Exception var9) {
            log.error("Error determining if user allowed from redis", var9);
            return false;
        }
    }

}
