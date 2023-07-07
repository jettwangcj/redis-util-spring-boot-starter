package cn.org.wangchangjiu.redis.query;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Optional;

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

        String scoreMethod = fieldRuleKey.getScoreMethod();
        Optional<Method> method = ReflectionUtils.getMethod(clazz, scoreMethod);
        if(!method.isPresent()){
            throw new RedisQueryException("");
        }

        if(!Double.class.equals(method.get().getReturnType())){
            throw new RedisQueryException("");
        }
        return fieldRuleKey;
    }

}
