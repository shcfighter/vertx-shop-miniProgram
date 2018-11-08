package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CouponSql {

    /**
     * 获取代金券列表
     */
    String SELECT_COUPON_SQL = "select coupon_name, coupon_type, coupon_amount, min_user_amount, expiry_date from t_coupon where begin_time <= ? and end_time >= ? and is_deleted = 0 order by sort";

}