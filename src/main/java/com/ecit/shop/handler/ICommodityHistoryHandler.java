package com.ecit.shop.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by shwang on 2018/2/2.
 */
@ProxyGen
@VertxGen
public interface ICommodityHistoryHandler {

    @Fluent
    ICommodityHistoryHandler findBrowsingHistory(String token, int page, int pageSize, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    ICommodityHistoryHandler insertBrowsingHistory(String token, JsonObject document, Handler<AsyncResult<String>> handler);

    @Fluent
    ICommodityHistoryHandler findCollectHistory(String token, int page, int pageSize, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    ICommodityHistoryHandler insertCollectHistory(String token, long commodityId, Handler<AsyncResult<String>> handler);

}
