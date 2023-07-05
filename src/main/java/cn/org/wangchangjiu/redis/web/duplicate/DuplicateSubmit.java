package cn.org.wangchangjiu.redis.web.duplicate;

import java.lang.annotation.*;

/**
 * @Classname DuplicateSubmit
 * @Description
 * @Date 2022/10/24 11:20
 * @Created by wangchangjiu
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DuplicateSubmit {

    /**
     *  防止重复提交控制时间 默认 1 s
     * @return
     */
   int interval() default 1;

}
