package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CommoditySql {

    /**
     * 获取banner信息
     */
    String SELECT_BANNER_SQL = "select * from t_ where is_show = 1 and is_deleted = 0 order by sort";

}