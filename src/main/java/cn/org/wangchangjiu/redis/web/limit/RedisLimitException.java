package cn.org.wangchangjiu.redis.web.limit;

public class RedisLimitException extends RuntimeException {

    public RedisLimitException(){}

    public RedisLimitException(String message){
        super(message);
    }

}
