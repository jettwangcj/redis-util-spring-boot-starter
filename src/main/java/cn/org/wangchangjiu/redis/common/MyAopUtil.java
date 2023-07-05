package cn.org.wangchangjiu.redis.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @Classname MyAopUtil
 * @Description
 * @Date 2023/7/5 10:47
 * @Created by wangchangjiu
 */
@Slf4j
public class MyAopUtil extends AopUtils {

    public static Method getCurrentMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 上面拿到的可能是接口的方法，所以需要以下的操作来确保获取到的是实现类的方法
        if (method.getDeclaringClass().isInterface()) {
            try {
                return joinPoint.getTarget().getClass().getDeclaredMethod(joinPoint.getSignature().getName(),
                        method.getParameterTypes());
            } catch (final SecurityException | NoSuchMethodException e) {
                log.error("无法获取当前的Method：{}", joinPoint.getSignature().getName());
                throw new RuntimeException("无法获取当前的Method", e);
            }
        }
        return method;
    }

    public static <T extends Annotation> T getMethodAnnotation(ProceedingJoinPoint joinPoint, Class<T> annotationClass){
        Method currentMethod = getCurrentMethod(joinPoint);
        if(currentMethod != null){
             AnnotationUtils.findAnnotation(currentMethod, annotationClass);
        }
        return null;
    }

}
