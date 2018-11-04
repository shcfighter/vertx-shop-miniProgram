package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CommoditySql {

    /**
     * 获取banner信息
     */
    String SELECT_BANNER_SQL = "select * from t_banner where is_show = 1 and is_deleted = 0 order by sort";

    /**
     * 查询商品价格
     */
    String COMMODITY_PRICE_SQL = "select price, num, commodity_id from t_commodity_attribute where commodity_id = ? and attribute_name = ? and is_deleted = 0";

}