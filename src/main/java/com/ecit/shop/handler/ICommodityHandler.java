package com.ecit.shop.handler;

import com.hubrick.vertx.elasticsearch.model.SearchResponse;
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
public interface ICommodityHandler {

    @Fluent
    ICommodityHandler category(Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    ICommodityHandler searchCommodity(String keyword, String category, int pageSize, int page, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityHandler findCommodityFromEsById(long id, Handler<AsyncResult<SearchResponse>> handler);

}
