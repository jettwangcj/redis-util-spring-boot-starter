package cn.org.wangchangjiu.redis.common;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @Classname SpElUtil
 * @Description
 * @Date 2023/7/5 11:19
 * @Created by wangchangjiu
 */
public class SpElUtil {

    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public static String parseSpEl(Method method, Object[] args, String spEl) {
        //解析参数名
        String[] params = Optional.ofNullable(parameterNameDiscoverer.getParameterNames(method)).orElse(new String[]{});

        //el解析需要的上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < params.length; i++) {
            //所有参数都作为原材料扔进去
            context.setVariable(params[i], args[i]);
        }
        Expression expression = parser.parseExpression(spEl);
        return expression.getValue(context, String.class);
    }

}
