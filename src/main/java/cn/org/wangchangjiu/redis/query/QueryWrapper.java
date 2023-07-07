package cn.org.wangchangjiu.redis.query;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Classname QueryWrapper
 * @Description
 * @Date 2023/7/4 18:00
 * @Created by wangchangjiu
 */
@Data
public class QueryWrapper implements Serializable {

   /**
    *  查询字段
    */
   private Map<String, String> queryFields;


   private Integer page;

   private Integer size;

   private boolean desc;

}
