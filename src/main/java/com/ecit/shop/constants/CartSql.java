package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CartSql {

    /**
     * 新增购物车信息
     */
    String INSERT_CART_SQL = "insert into t_cart(cart_id, user_id, commodity_id, commodity_name, num, price, image_url, specifition_name, freight_price, is_deleted, create_time, versions) values(?, ?, ?, ?, ?, ?, ?, ?, ?, 0, now(), 0)";

    /**
     * 查询购物车列表
     */
    String SELECT_CART_SQL = "select cart_id::text, user_id, commodity_id, commodity_name, num, price::numeric, image_url, specifition_name, freight_price::numeric, create_time, versions from t_cart where user_id = ? and is_deleted = 0 order by create_time desc";

    /**
     * 根据cart_id查询
     */
    String SELECT_CART_BY_ID_SQL = "select cart_id::text, user_id, commodity_id, commodity_name, num, price::numeric, image_url, specifition_name, freight_price::numeric, create_time, versions from t_cart where user_id = ? and cart_id = ? and is_deleted = 0 order by create_time desc";

    /**
     * 查询商品数量
     */
    String SELECT_CART_BY_SPECIFITION_SQL = "select cart_id::text, user_id, commodity_id, commodity_name, num, price::numeric, image_url, specifition_name, create_time, versions from t_cart where user_id = ? and commodity_id = ? and specifition_name = ? and is_deleted = 0 ";

    /**
     * 修改购物车商品数量
     */
    String UPDAT_CART_NUM_SQL = "update t_cart set num = ?, price = ?, versions = (versions + 1), update_time = now() where user_id = ? and commodity_id = ? and specifition_name = ? and versions = ? and is_deleted = 0";

    /**
     * 删除购物车记录
     */
    String DELETE_CART_SQL = "update t_cart set is_deleted = 1, versions = (versions + 1), update_time = now() where user_id = ? and cart_id = ? and versions = ? and is_deleted = 0";

    /**
     * 批量删除购物车记录
     */
    String BATCH_DELETE_CART_SQL = "update t_cart set is_deleted = 1, versions = (versions + 1), update_time = now() where user_id = ? and cart_id in({{carts}}) and is_deleted = 0";

    /**
     *  购物车数量
     */
    String SELECT_ROWNUM_CART_SQL = "select sum(num) row_num from t_cart where user_id = ? and is_deleted = 0";
}