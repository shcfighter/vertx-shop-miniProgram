package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CommoditySql {

    /**
     * 查询商品价格
     */
    String SELECT_COMMODITY_SQL = "select price::numeric, num, commodity_id::text from t_commodity where commodity_id = ? and status = 1 and is_deleted = 0";

    /**
     * 查询商品价格
     */
    String COMMODITY_PRICE_SQL = "select price::numeric, num, commodity_id from t_commodity_specifition where commodity_id = ? and specifition_name = ? and is_deleted = 0";

}