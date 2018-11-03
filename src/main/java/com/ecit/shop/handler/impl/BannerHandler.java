package com.ecit.shop.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.shop.constants.BannerSql;
import com.ecit.shop.handler.IBannerHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import java.util.List;

public class BannerHandler extends JdbcRxRepositoryWrapper implements IBannerHandler {

    public BannerHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public IBannerHandler banner(Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<List<JsonObject>> addressFuture = Future.future();
        this.retrieveAll(BannerSql.SELECT_BANNER_SQL).subscribe(addressFuture::complete, addressFuture::fail);
        addressFuture.setHandler(handler);
        return this;
    }
}
