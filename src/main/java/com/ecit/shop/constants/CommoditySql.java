package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CommoditySql {

    /**
     * 查询商品价格
     */
    String SELECT_COMMODITY_SQL = "select price::numeric, num, commodity_id::text, commodity_name, status, freight_price::numeric, image_url, versions from t_commodity where commodity_id = ? and status = 1 and is_deleted = 0";

    /**
     * 查询商品详情
     */
    String SELECT_COMMODITY_SPECIFITION_SQL = "select c.price::numeric, cs.num, c.commodity_id::text, c.commodity_name, c.status, c.freight_price::numeric, c.image_url, cs.versions from t_commodity c LEFT JOIN t_commodity_specifition cs on(c.commodity_id = cs.commodity_id) where c.commodity_id = ? and c.status = 1 and c.is_deleted = 0 and cs.is_deleted = 0";

    /**
     * 查询商品价格（规格）
     */
    String COMMODITY_PRICE_SQL = "select price::numeric, num, commodity_id::text, commodity_name, status, freight_price::numeric, specifition_name, versions from t_commodity_specifition where commodity_id = ? and specifition_name = ? and status = 1 and is_deleted = 0";

    /**
     * 购买商品
     */
    String UPDATE_BUY_COMMODITY_SQL = "update t_commodity set num = (num - ?), versions = (versions + 1), update_time = now() where commodity_id = ? and versions = ? and status = 1 and is_deleted = 0";

    /**
     * 购买商品（规格）
     */
    String UPDATE_BUY_COMMODITY_SPECIFITION_SQL = "update t_commodity_specifition set num = (num - ?), versions = (versions + 1), update_time = now() where commodity_id = ? and specifition_name = ? and versions = ? and status = 1 and is_deleted = 0";
}