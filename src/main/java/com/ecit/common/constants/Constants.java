package com.ecit.common.constants;

/**
 * Created by shwang on 2018/3/25.
 */
public class Constants {

    /**
     * 默认cookie key
     */
    public static final String VERTX_WEB_SESSION = "vertx-shop.session";

    /**
     * session过期时间
     */
    public static final long SESSION_EXPIRE_TIME = 24 * 60 * 60;

    /**
     * es商品索引indeces
     */
    public static final String SHOP_INDICES = "shop_miniprogram";

    /**
     * mongodb 商品浏览记录collection
     */
    public static final String MONGO_COLLECTION_COMDITIDY_BROWSE = "miniprogram_comditidy_browse";

    /**
     * mongodb 商品收藏记录collection
     */
    public static final String MONGO_COLLECTION_COMDITIDY_COLLECT = "miniprogram_comditidy_collect";
}
