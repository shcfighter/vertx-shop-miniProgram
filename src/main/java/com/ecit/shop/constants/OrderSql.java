package com.ecit.shop.constants;

/**
 * 订单
 */
public interface OrderSql {

    /**
     * 下单
     */
    String INSERT_ORDER_SQL = "insert into t_order(order_id, user_id, country_id, province_id, city_id, district_id, address, mobile, postcode, create_time, order_details, total_price, order_status, coupon_id, coupon_price, freight_price) values(?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?)";
}
