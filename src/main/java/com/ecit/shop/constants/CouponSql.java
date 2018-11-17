package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CouponSql {

    /**
     * 获取代金券列表
     */
    String SELECT_COUPON_SQL = "select coupon_id::text, coupon_name, coupon_type, coupon_amount::numeric, min_use_amount::numeric, expiry_date, grant_num from t_coupon where grant_num > 0 and begin_time <= ? and end_time >= ? and is_deleted = 0 order by sort";

    /**
     * 通过id查询代金券
     */
    String SELECT_COUPON_BY_ID_SQL = "select coupon_id, coupon_name, coupon_type, coupon_amount::numeric, begin_time, end_time, min_use_amount, expiry_date, category_id, category_name, is_deleted, grant_num, versions from t_coupon where coupon_id = ? and is_deleted = 0";

    /**
     * 修改代金券可用数量
     */
    String UPDATE_COUPON_NUM_SQL = "update t_coupon set grant_num = (grant_num - 1), update_time = now(), versions = (versions + 1) where coupon_id = ? and is_deleted = 0";

    /**
     * 根据id查询代金券详情
     */
    String SELECT_COUPON_DETAIL_BY_ID_SQL = "select coupon_detail_id::text, coupon_name, coupon_type, coupon_amount::numeric, category_id::text, begin_time, end_time, user_id, min_use_amount, expiry_date, is_use, versions from t_coupon_detail where user_id = ? and coupon_detail_id = ? and is_deleted = 0";

    /**
     * 根据用户id、代金券id查询是否领取
     */
    String SELECT_COUPON_DETAIL_USERID_COUPONID_SQL = "select count(1) row_num from t_coupon_detail where user_id = ? and coupon_id = ? and is_deleted = 0";

    /**
     * 领取代金券
     */
    String INSERT_COUPON_DETAIL_SQL = "insert into t_coupon_detail(coupon_detail_id, coupon_id, coupon_name, coupon_type, coupon_amount, category_id, category_name, begin_time, end_time, user_id, min_use_amount, expiry_date, create_time) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())";

    /**
     * 根据用户id查询可用代金券
     */
    String SELECT_COUPON_DETAIL_USERID_SQL = "select coupon_detail_id::text, coupon_name, coupon_amount::numeric, begin_time, end_time, min_use_amount::numeric, versions from t_coupon_detail where user_id = ? and begin_time <= ? and end_time >= ? and is_deleted = 0 and is_use = 0 ";

    /**
     * 可用代金券数量
     */
    String SELECT_COUPON_DETAIL_ROWNUM_SQL = "select count(1) row_num from t_coupon_detail where user_id = ? and begin_time <= ? and end_time >= ? and is_deleted = 0 and is_use = 0 ";

    /**
     * 根据用户、代金券状态查询代金券
     */
    String SELECT_COUPON_DETAIL_USERID_STATUS_SQL = "select coupon_detail_id::text, coupon_name, coupon_amount::numeric, begin_time, end_time, min_use_amount::numeric, versions from t_coupon_detail where user_id = ? and is_use = ? and begin_time <= ? and end_time >= ? and is_deleted = 0";

    /**
     * 使用代金券
     */
    String UPDATE_COUPON_USER_SQL = "update t_coupon_detail set is_use = 1, versions = (versions + 1), update_time = now() where coupon_detail_id = ? and user_id = ? and is_use = 0 and is_deleted = 0";
}