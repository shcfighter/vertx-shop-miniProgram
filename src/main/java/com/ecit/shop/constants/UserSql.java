package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface UserSql {

    /**
     * 通过微信openid查询用户
     */
    String SELECT_BY_OPENID_SQL = "select * from t_user where open_id = ? and status = 1 and is_deleted = 0";

    /**
     * 通过token查询用户
     */
    String SELECT_BY_TOKEN_SQL = "select user_id, open_id, token from t_user where token = ? and status = 1 and is_deleted = 0";

    /**
     * 通过微信openid新增用户
     */
    String INSERT_USER_BY_OPENID_SQL = "insert into t_user(user_id, open_id, token, status, is_deleted, create_time, versions) values(?, ?, ?, ?, 0, now(), 0)";

    /**
     * 修改用户的token信息
     */
    String UPDATE_USER_TOKEN_SQL = "update t_user set token = ?, versions = (versions + 1) where user_id = ? and versions = ?";

    /**
     * 判断token是否存在
     */
    String CHECK_USER_TOKEN_SQL = "select count(1) num from t_user where token = ? and is_deleted = 0";


}