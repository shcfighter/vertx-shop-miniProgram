package com.ecit.shop.handler.impl;

import com.ecit.common.IdBuilder;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.shop.constants.CartSql;
import com.ecit.shop.handler.ICartHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import java.util.List;
import java.util.Objects;

public class CartHandler extends JdbcRxRepositoryWrapper implements ICartHandler {

    public CartHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public ICartHandler insertCart(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            final long userId = session.getLong("user_id");
            Future<JsonObject> cartFuture = Future.future();
            this.retrieveOne(new JsonArray().add(userId).add(params.getLong("commodity_id")).add(params.getString("specifition_name")),
                    CartSql.SELECT_CART_SQL).subscribe(cartFuture::complete, cartFuture::fail);
            return cartFuture.compose(cart -> {
                Future<Integer> resultFuture = Future.future();
                if(Objects.isNull(cart) || cart.isEmpty()){
                    this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(params.getLong("commodity_id"))
                            .add(params.getString("commodity_name")).add(params.getInteger("num")).add(params.getString("price"))
                            .add(params.getString("image_url")).add(params.getString("specifition_name")), CartSql.INSERT_CART_SQL).
                            subscribe(resultFuture::complete, resultFuture::fail);
                }else{
                    this.execute(new JsonArray().add(params.getInteger("num")).add(params.getString("price")).add(userId)
                            .add(params.getLong("commodity_id")).add(params.getString("specifition_name"))
                            .add(cart.getLong("versions")), CartSql.UPDAT_CART_NUM_SQL).
                            subscribe(resultFuture::complete, resultFuture::fail);
                }
                return resultFuture;
            });
        }).setHandler(handler);
        return this;
    }

    @Override
    public ICartHandler cartList(String token, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<List<JsonObject>> resultFuture = Future.future();
            this.retrieveMany(new JsonArray().add(userId), CartSql.SELECT_CART_SQL).subscribe(resultFuture::complete, resultFuture::fail);
            return resultFuture;
        }).setHandler(handler);
        return this;
    }

    @Override
    public ICartHandler findCart(String token, JsonObject params, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<JsonObject> resultFuture = Future.future();
            this.retrieveOne(new JsonArray().add(userId).add(params.getLong("commodity_id")).add(params.getString("specifition_name")),
                    CartSql.SELECT_CART_SQL).subscribe(resultFuture::complete, resultFuture::fail);
            return resultFuture;
        }).setHandler(handler);
        return this;
    }
}
