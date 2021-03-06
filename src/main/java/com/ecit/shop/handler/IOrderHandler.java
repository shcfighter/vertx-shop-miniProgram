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
public interface IOrderHandler {

    @Fluent
    IOrderHandler insertOrder(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IOrderHandler orderList(String token, int status, int page, int pageSize, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IOrderHandler orderDetail(String token, long orderId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderHandler cancelOrder(String token, long orderId, Handler<AsyncResult<Integer>> handler);
}
