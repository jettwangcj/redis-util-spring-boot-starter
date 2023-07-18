package cn.org.wangchangjiu.redis.web.limit.api;

import cn.org.wangchangjiu.redis.web.limit.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class ApiConfigResolver implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private RedisLimitProperties redisLimitProperties;

    private static final Map<String, Config> configMap = new HashMap<>();

    public ApiConfigResolver(RedisLimitProperties redisLimitProperties){
        this.redisLimitProperties = redisLimitProperties;
    }

    public void initMethod(){
        List<RedisLimitProperties.Config> configs = redisLimitProperties.getConfigs();
        if(!CollectionUtils.isEmpty(configs)){
            configs.stream().forEach(config -> {
                KeyResolver keyResolver = this.applicationContext.getBean(config.getKeyResolver(), KeyResolver.class);
                Config f = new Config(config.getPath(), config.getReplenishRate(), config.getBurstCapacity(), keyResolver);
                configMap.put(f.getPath(), f);
            });
        }
    }

    public Config mathConfig(String requestURI) {
        if(!CollectionUtils.isEmpty(configMap.keySet())){
            String path = CommonUtils.getOptimalPath(new ArrayList<>(configMap.keySet()), requestURI);
            if(StringUtils.hasText(path)){
               return configMap.get(path);
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Config {

        protected String path;

        /**
         * 令牌桶美秒填充速率
         */
        protected int replenishRate;

        /**
         * 令牌桶容量
         */
        protected int burstCapacity;

        /**
         *
         */
        protected KeyResolver keyResolver;

    }



}
