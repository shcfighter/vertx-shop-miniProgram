package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface IntegrationSql {

    /**
     * 查询用户积分
     */
    String SELECT_INTEGRATION_SQL = "select sum(integration) row_num from t_integration where user_id = ? and is_deleted = 0";

    /**
     * 根据用户查询积分信息
     */
    String SELECT_INTEGRATION_BY_USER_SQL = "select * from t_integration where user_id = ? and is_deleted = 0";

    /**
     * 新增积分
     */
    String INSERT_INTEGRATION_SQL = "insert into t_integration(integration_id, user_id, integration, user_integration, create_time) values(?, ?, ?, 0, now())";

    /**
     * 修改积分信息
     */
    String UPDATE_INTEGRATION_SQL = "update t_integration set integration = (integration + ?), versions = (versions + 1) where user_id = ? and versions = ?";

}