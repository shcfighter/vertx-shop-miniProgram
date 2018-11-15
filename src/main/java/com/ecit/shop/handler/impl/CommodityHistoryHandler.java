package com.ecit.shop.handler.impl;

import com.ecit.common.constants.Constants;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.shop.constants.CommoditySql;
import com.ecit.shop.handler.ICommodityHistoryHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.reactivex.core.Vertx;

import java.util.List;

/**
 * 商品浏览记录、商品收藏记录
 */
public class CommodityHistoryHandler extends JdbcRxRepositoryWrapper implements ICommodityHistoryHandler {
    public CommodityHistoryHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public ICommodityHistoryHandler findBrowsingHistory(String token, int page, int pageSize, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<List<JsonObject>> future = Future.future();
            JsonObject query = new JsonObject().put("user_id", userId).put("is_deleted", 0);
            mongoClient.rxFindWithOptions(Constants.MONGO_COLLECTION_COMDITIDY_BROWSE, query, new FindOptions().setLimit(pageSize).setSkip(((page - 1) * pageSize))
                    .setSort(new JsonObject().put("create_time", -1)))
                    .subscribe(future::complete, future::fail);
            return future;
        }).setHandler(handler);
        return this;
    }

    @Override
    public ICommodityHistoryHandler insertBrowsingHistory(String token, JsonObject commodity, Handler<AsyncResult<String>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            JsonObject document = new JsonObject().put("commodity", commodity).put("user_id", userId).put("is_deleted", 0).put("create_time", System.currentTimeMillis());
            Future<String> future = Future.future();
            mongoClient.rxInsert(Constants.MONGO_COLLECTION_COMDITIDY_BROWSE, document)
                    .subscribe(future::complete, future::fail);
            return future;
        }).setHandler(handler);
        return this;
    }

    @Override
    public ICommodityHistoryHandler findCollectHistory(String token, int page, int pageSize, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<List<JsonObject>> future = Future.future();
            JsonObject query = new JsonObject().put("user_id", userId).put("is_deleted", 0);
            mongoClient.rxFindWithOptions(Constants.MONGO_COLLECTION_COMDITIDY_COLLECT, query, new FindOptions().setLimit(pageSize).setSkip(((page - 1) * pageSize))
                    .setSort(new JsonObject().put("create_time", -1)))
                    .subscribe(future::complete, future::fail);
            return future;
        }).setHandler(handler);
        return this;
    }

    @Override
    public ICommodityHistoryHandler insertCollectHistory(String token, long commodityId, Handler<AsyncResult<String>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<JsonObject> collectFuture = Future.future();
            mongoClient.rxFindOneAndDelete(Constants.MONGO_COLLECTION_COMDITIDY_COLLECT, new JsonObject().put("user_id", userId).put("commodity_id", commodityId).put("is_deleted", 0))
                    .subscribe(collectFuture::complete, collectFuture::fail);
            return collectFuture.compose(collect -> {
                System.out.println("============================================================");
                System.out.println(collect);
               if(JsonUtils.isNull(collect)){
                   Future<JsonObject> commodityFuture = Future.future();
                   this.retrieveOne(new JsonArray().add(commodityId), CommoditySql.SELECT_COMMODITY_BY_ID_SQL).subscribe(commodityFuture::complete, commodityFuture::fail);
                   return commodityFuture.compose(commodity -> {
                       if(JsonUtils.isNull(commodity)){
                           return Future.failedFuture("商品不存在");
                       }
                       mongoClient.rxInsert(Constants.MONGO_COLLECTION_COMDITIDY_COLLECT,
                               new JsonObject().put("commodity", commodity).put("commodity_id", commodity.getLong("commodity_id"))
                                       .put("user_id", userId).put("is_deleted", 0).put("create_time", System.currentTimeMillis())).subscribe();
                       return Future.succeededFuture("success");
                   });
               }
               return Future.succeededFuture("success");
            });
        }).setHandler(handler);
        return this;
    }
}
