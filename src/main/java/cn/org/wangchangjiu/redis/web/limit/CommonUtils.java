package cn.org.wangchangjiu.redis.web.limit;

import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @Classname CommonUtil
 * @Description
 * @Date 2023/7/17 18:15
 * @Created by wangchangjiu
 */
public class CommonUtils {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    public static String getOptimalPath(List<String> paths, String path) {
        // 先找到匹配的url
        List<String> matchingPatterns = new ArrayList<>();
        for (String pattern : paths) {
            // 判断该url是否与目标匹配，匹配的话加入到list
            if (MATCHER.match(pattern, path)) {
                matchingPatterns.add(pattern);
            }
        }
        // 从匹配的url中选取一个最合适的
        if (!matchingPatterns.isEmpty()) {
            Comparator<String> comparator = MATCHER.getPatternComparator(path);
            // 进行排序
            matchingPatterns.sort(comparator);
            // 拿到第一个最匹配的
            return matchingPatterns.get(0);
        }
        return null;
    }

}
