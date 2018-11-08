package com.ecit.shop.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by shwang on 2018/2/2.
 */
@ProxyGen
@VertxGen
public interface IOrderHandler {

    @Fluent
    IOrderHandler insertOrder(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

}