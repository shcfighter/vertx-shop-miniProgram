package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CartSql {

    /**
     * 新增购物车信息
     */
    String INSERT_CART_SQL = "insert into t_cart(cart_id, user_id, commodty_id, commodity_name, num, price, image_url, specifition_name, is_deleted, create_time, versions) values(?, ?, ?, ?, ?, ?, ?, ?, 0, now(), 0)";

    /**
     * 查询购物车列表
     */
    String SELECT_CART_SQL = "select cart_id, user_id, commodty_id, commodity_name, num, price::numeric, image_url, specifition_name, create_time, versions from t_cart where user_id = ? and is_deleted = 0 order by create_time desc";

}