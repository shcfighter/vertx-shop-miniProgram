package com.ecit.shop.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface CategorySql {

    /**
     * 获取商品类别信息
     */
    String SELECT_CATEGORY_SQL = "select category_id::text, category_name, sort, versions from t_category where is_deleted = 0 order by sort asc";

}