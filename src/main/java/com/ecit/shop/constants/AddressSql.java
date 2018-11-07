package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface AddressSql {


    /**
     * 新增收货地址
     */
    String INSERT_ADDRESS_SQL = "insert into t_address(address_id, user_id, name, province_id, city_id, district_id, address, mobile, code, is_default, is_deleted, create_time, versions) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, now(), 0)";

    /**
     *  更新收货地址
     */
    String UPDATE_ADDRESS_SQL = "update t_address set name = ?, province_id = ?, city_id = ?, district_id = ?, address = ?, mobile = ?, code = ?, update_time = now(), versions = (versions + 1) where address_id = ? and versions = ?";

    /**
     * 查询收货地址信息
     */
    String SELECT_ADDRESS_BY_ID_SQL = "select address_id::text, name, province_id, city_id, district_id, address, mobile, code, is_default, versions from t_address where address_id = ? and user_id = ? and is_deleted = 0";

    /**
     * 逻辑删除收货地址
     */
    String UPDATE_DELETE_ADDRESS_SQL = "update t_address set is_deleted = 1, update_time = now(), versions = (versions + 1) where address_id = ? and versions = ?";

    /**
     * 查询收货地址列表
     */
    String SELECT_ADDRESS_BY_USERID_SQL = "select address_id::text, name, province_id, city_id, district_id, address, mobile, code, is_default, versions from t_address where user_id = ? and is_deleted = 0 order by update_time desc";

    /**
     * 获取默认收货地址
     */
    String SELECT_DEFAULT_ADDRESS_SQL = "select address_id::text, name, province_id, city_id, district_id, address, mobile, code, is_default, versions from t_address where user_id = ? and is_deleted = 0 order by update_time desc";


}