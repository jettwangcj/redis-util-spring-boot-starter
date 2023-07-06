package cn.org.wangchangjiu.redis.query;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

/**
 * @Classname QueryUtil
 * @Description
 * @Date 2023/7/6 11:21
 * @Created by wangchangjiu
 */
public class QueryUtil {

    public static FieldRuleKey checkGetFieldRuleKey(Class clazz){
        FieldRuleKey fieldRuleKey = AnnotationUtils.findAnnotation(clazz, FieldRuleKey.class);
        if(fieldRuleKey == null){
            throw new RedisQueryException("");
        }

        /*String keyName = fieldRuleKey.keyName();
        if(!StringUtils.hasText(keyName)){
            throw new RedisQueryException("");
        }*/

        return fieldRuleKey;
    }

}
