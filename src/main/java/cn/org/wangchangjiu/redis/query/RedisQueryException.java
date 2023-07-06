package cn.org.wangchangjiu.redis.query;

/**
 * @Classname RedisQueryException
 * @Description
 * @Date 2023/7/6 11:17
 * @Created by wangchangjiu
 */
public class RedisQueryException extends RuntimeException {

    public RedisQueryException(){}

    public RedisQueryException(String message){
        super(message);
    }

}
