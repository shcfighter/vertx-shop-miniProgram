package com.ecit.shop.constants;

/**
 * 订单
 */
public interface OrderSql {

    /**
     * 下单
     */
    String INSERT_ORDER_SQL = "insert into t_order(order_id, user_id, country_id, province_id, city_id, district_id, receiver, address, mobile, postcode, create_time, order_details, total_price, order_status, coupon_id, coupon_price, freight_price, actual_price, remarks) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * 分页查询订单列表
     */
    String SELECT_ORDER_PAGE_SQL = "select order_id::text, actual_price::numeric, total_price::numeric, order_details, create_time from t_order where user_id = ? and order_status = ? and is_deleted = 0 order by create_time desc limit ? offset ?";

    /**
     * 分页查询全部订单列表
     */
    String SELECT_ORDER_PAGE_ALL_SQL = "select order_id::text, actual_price::numeric, total_price::numeric, order_details, create_time from t_order where user_id = ? and is_deleted = 0 order by create_time desc limit ? offset ?";

    /**
     * 根据id查询订单
     */
    String SELECT_ORDER_BY_ID_SQL = "select * from t_order where order_id = ? and user_id = ? and is_deleted = 0";
}
