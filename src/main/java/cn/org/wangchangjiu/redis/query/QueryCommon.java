package cn.org.wangchangjiu.redis.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname QueryCommon
 * @Description
 * @Date 2023/7/7 16:36
 * @Created by wangchangjiu
 */
public class QueryCommon {

    public static final String ZSET_KEY = "%s_%s";

    public static List<String> generateAllKey(String keyName, String ruleKey) {
        String[] ruleKeys = ruleKey.split(":");
        StringBuilder ruleKeyBuilder = new StringBuilder();
        List<String> generateKey = new ArrayList<>();
        for(int i = 0 ; i < ruleKeys.length ; i++){
            for(int j = 0; j < ruleKeys.length ; j++ ){
                if(i == j){
                    ruleKeyBuilder.append(ruleKeys[i]);
                } else {
                    ruleKeyBuilder.append("*");
                }
            }
            String zsetKey = String.format(ZSET_KEY, keyName, ruleKeyBuilder);
            generateKey.add(zsetKey);
            // 清除数据
            ruleKeyBuilder.setLength(0);
        }
        return generateKey;
    }

}
