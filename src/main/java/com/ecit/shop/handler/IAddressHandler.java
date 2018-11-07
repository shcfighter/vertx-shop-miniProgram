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
public interface IAddressHandler {

    @Fluent
    IAddressHandler getAddressById(String token, long id, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IAddressHandler insertAddress(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler updateAddress(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler delAddress(String token, long addressId, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler listAddress(String token, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IAddressHandler findDefaultAddress(String token, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IAddressHandler updateDefaultAddress(String token, long addressId, Handler<AsyncResult<Integer>> handler);

}
