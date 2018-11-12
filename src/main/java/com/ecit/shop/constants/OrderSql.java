package com.ecit.shop.constants;

/**
 * 订单
 */
public interface OrderSql {

    /**
     * 下单
     */
    String INSERT_ORDER_SQL = "insert into t_order(order_id, user_id, country_id, province_id, city_id, district_id, address, mobile, postcode, create_time, order_details, total_price, order_status, coupon_id, coupon_price, freight_price, actual_price) values(?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?)";

    /**
     * 查询订单列表
     */
    String SELECT_ORDER_SQL = "select order_id::text, actual_price::numeric, total_price::numeric, order_details from t_order where user_id = ? and is_deleted = 0";
}
