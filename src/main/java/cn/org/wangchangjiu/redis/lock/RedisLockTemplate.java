package cn.org.wangchangjiu.redis.lock;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisLockTemplate
 * @Description
 * @Date 2023/7/5 15:02
 * @Created by wangchangjiu
 */

@Slf4j
public class RedisLockTemplate {

    private static final long DEFAULT_WAIT_TIME = 30 * 1000;
    private static final long DEFAULT_TIMEOUT   = 5 * 1000;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    private RedissonClient redissonClient;

    public RedisLockTemplate(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }



    public <T> T lock(Task<T> task, boolean fairLock) {
        return lock(task, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, fairLock);
    }


    public <T> T lock(Task<T> task, long leaseTime, TimeUnit timeUnit, boolean fairLock) {
        RLock lock = getLock(task.getLockName(), fairLock);
        try {
            lock.lock(leaseTime, timeUnit);
            return task.execute();
        } finally {
            if (lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }
    }


    public <T> T tryLock(Task<T> task, boolean fairLock) {
        return tryLock(task, DEFAULT_WAIT_TIME, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, fairLock);
    }


    public <T> T tryLock(Task<T> task, long waitTime, long leaseTime, TimeUnit timeUnit, boolean fairLock) {
        RLock lock = getLock(task.getLockName(), fairLock);
        try {
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                return task.execute();
            }
        } catch (InterruptedException e) {

        } finally {
            if (lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }
        return null;
    }

    private RLock getLock(String lockName, boolean fairLock) {
        RLock lock;
        if (fairLock) {
            lock = redissonClient.getFairLock(lockName);
        } else {
            lock = redissonClient.getLock(lockName);
        }
        return lock;
    }

    public interface Task<T> {
        T execute();

        String getLockName();
    }

}
