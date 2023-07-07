package cn.org.wangchangjiu.redis.query;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Classname FieldRuleKey
 * @Description
 * @Date 2023/7/6 11:10
 * @Created by wangchangjiu
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FieldRuleKey {

   String keyName() default "";

   String[] queryFields() default {};

   int expire() default -1;

   TimeUnit timeUnit() default TimeUnit.SECONDS;

   String getScoreMethod() default "getScore";
}
