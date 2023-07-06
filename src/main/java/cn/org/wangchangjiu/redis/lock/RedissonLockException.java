package cn.org.wangchangjiu.redis.lock;

/**
 * @Classname RedissonLockException
 * @Description
 * @Date 2023/7/5 11:58
 * @Created by wangchangjiu
 */
public class RedissonLockException extends RuntimeException {
    public RedissonLockException() {
    }

    public RedissonLockException(String message) {
        super(message);
    }

}
