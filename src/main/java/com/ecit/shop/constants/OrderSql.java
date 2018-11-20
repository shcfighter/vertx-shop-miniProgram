package com.ecit.shop.constants;

/**
 * 订单
 */
public interface OrderSql {

    /**
     * 下单
     */
    String INSERT_ORDER_SQL = "insert into t_order(order_id, user_id, province_id, city_id, district_id, province_value, city_value, district_value, receiver, address, mobile, postcode, create_time, order_details, total_price, order_status, coupon_id, coupon_price, freight_price, actual_price, remarks) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * 分页查询订单列表
     */
    String SELECT_ORDER_PAGE_SQL = "select order_id::text, actual_price::numeric, total_price::numeric, order_details, order_status, create_time from t_order where user_id = ? and order_status = ? and is_deleted = 0 order by create_time desc limit ? offset ?";

    /**
     * 分页查询全部订单列表
     */
    String SELECT_ORDER_PAGE_ALL_SQL = "select order_id::text, actual_price::numeric, total_price::numeric, order_details, order_status, create_time from t_order where user_id = ? and is_deleted = 0 order by create_time desc limit ? offset ?";

    /**
     * 根据id查询订单
     */
    String SELECT_ORDER_BY_ID_SQL = "select order_id, province_id, city_id, district_id, province_value, city_value, district_value, address, mobile, postcode, create_time, order_details, total_price::numeric, order_status, coupon_price::numeric, pay_time, cancel_time, send_time, freight_price::numeric, actual_price::numeric, receiver, versions from t_order where order_id = ? and user_id = ? and is_deleted = 0";

    /**
     * 取消订单
     */
    String UPDATE_REFUND_ORDER_SQL = "update t_order set order_status = 6, cancel_time = now(), versions = (versions + 1) where user_id = ? and order_id = ? and versions = ? and order_status != -1 and order_status != 6 and order_status != 7";
}
