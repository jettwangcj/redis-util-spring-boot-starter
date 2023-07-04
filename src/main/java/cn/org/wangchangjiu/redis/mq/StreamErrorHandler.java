package cn.org.wangchangjiu.redis.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ErrorHandler;

/**
 * @Classname StreamErrorHandler
 * @Description
 * @Date 2023/7/4 14:24
 * @Created by wangchangjiu
 */
@Slf4j
public class StreamErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        log.error(" redis message handle error:{}", t.getMessage());
    }
}
