package com.ecit.shop.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.shop.constants.CategorySql;
import com.ecit.shop.handler.ICommodityHandler;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by shwang on 2018/2/2.
 */
public class CommodityHandler extends JdbcRxRepositoryWrapper implements ICommodityHandler {

    private static final Logger LOGGER = LogManager.getLogger(CommodityHandler.class);
    /**
     * es商品索引indeces
     */
    private static final String SHOP_INDICES = "shop";

    final RxElasticSearchService rxElasticSearchService;

    final Vertx vertx;
    final JsonObject config;
    public CommodityHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.config = config;
        rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx.getDelegate(), config.getString("address"));
    }


    @Override
    public ICommodityHandler category(Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<List<JsonObject>> future = Future.future();
        this.retrieveAll(CategorySql.SELECT_CATEGORY_SQL).subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
