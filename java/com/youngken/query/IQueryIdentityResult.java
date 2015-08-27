package com.youngken.query;

import com.esri.core.tasks.identify.IdentifyResult;

import java.util.List;

/**
 * Created by Young Ken on 2015/8/19.
 * 查询返回值接口
 */
public interface IQueryIdentityResult
{
   /**
    * @param list 查找结果集返回list
    */
   void setRestut(List<IdentifyResult> list);
}
