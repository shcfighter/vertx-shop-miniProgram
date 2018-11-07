package com.ecit.shop.handler.impl;

import com.ecit.common.IdBuilder;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.shop.constants.AddressSql;
import com.ecit.shop.handler.IAddressHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class AddressHandler extends JdbcRxRepositoryWrapper implements IAddressHandler {

    private static final Logger LOGGER = LogManager.getLogger(AddressHandler.class);
    final Vertx vertx;
    final JsonObject config;

    public AddressHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.config = config;
    }

    @Override
    public IAddressHandler getAddressById(String token, long id, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            final long userId = session.getLong("user_id");
            Future<JsonObject> future = Future.future();
            this.retrieveOne(new JsonArray().add(id).add(userId), AddressSql.SELECT_ADDRESS_BY_ID_SQL).subscribe(future::complete, future::fail);
            return future;
        }).setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler insertAddress(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            final long userId = session.getLong("user_id");
            Future<Integer> resultFuture = Future.future();
            this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(params.getString("name")).add(params.getInteger("province_id"))
                    .add(params.getInteger("city_id")).add(Objects.isNull(params.getInteger("district_id")) ? 0 : params.getInteger("district_id"))
                    .add(params.getString("address")).add(params.getString("mobile")).add(params.getString("code"))
                    .add(params.getInteger("is_default")), AddressSql.INSERT_ADDRESS_SQL).
                    subscribe(resultFuture::complete, resultFuture::fail);
            return resultFuture;
        }).setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler updateAddress(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            final long userId = session.getLong("user_id");
            Future<JsonObject> addressFuture = Future.future();
            this.retrieveOne(new JsonArray().add(params.getString("id")).add(userId), AddressSql.SELECT_ADDRESS_BY_ID_SQL).subscribe(addressFuture::complete, addressFuture::fail);
            return addressFuture.compose(address -> {
                Future<Integer> resultFuture = Future.future();
                if(Objects.isNull(address) || address.isEmpty()){
                    resultFuture.complete(0);
                    return resultFuture;
                }
                this.execute(new JsonArray().add(params.getString("name")).add(params.getInteger("province_id"))
                        .add(params.getInteger("city_id")).add(Objects.isNull(params.getInteger("district_id")) ? 0 : params.getInteger("district_id"))
                        .add(params.getString("address")).add(params.getString("mobile")).add(params.getString("code")).add(params.getString("id"))
                        .add(address.getLong("versions")), AddressSql.UPDATE_ADDRESS_SQL).subscribe(resultFuture::complete, resultFuture::fail);
                return resultFuture;
            });
        }).setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler delAddress(String token, long addressId, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<JsonObject> addressFuture = Future.future();
            this.retrieveOne(new JsonArray().add(addressId).add(userId), AddressSql.SELECT_ADDRESS_BY_ID_SQL).subscribe(addressFuture::complete, addressFuture::fail);
            return addressFuture.compose(address -> {
                Future<Integer> resultFuture = Future.future();
                if(Objects.isNull(address) || address.isEmpty()){
                    resultFuture.complete(0);
                    return resultFuture;
                }
                this.execute(new JsonArray().add(addressId).add(address.getLong("versions")), AddressSql.UPDATE_DELETE_ADDRESS_SQL)
                        .subscribe(resultFuture::complete, resultFuture::fail);
                return resultFuture;
            });
        }).setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler listAddress(String token, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<List<JsonObject>> resultFuture = Future.future();
            this.retrieveMany(new JsonArray().add(userId), AddressSql.SELECT_ADDRESS_BY_USERID_SQL).subscribe(resultFuture::complete, resultFuture::fail);
            return resultFuture;
        }).setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler findDefaultAddress(String token, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<JsonObject> resultFuture = Future.future();
            this.retrieveOne(new JsonArray().add(userId), AddressSql.SELECT_DEFAULT_ADDRESS_SQL).subscribe(resultFuture::complete, resultFuture::fail);
            return resultFuture;
        }).setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler updateDefaultAddress(String token, long addressId, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<JsonObject> addressFuture = Future.future();
            this.retrieveOne(new JsonArray().add(addressId).add(userId), AddressSql.SELECT_ADDRESS_BY_ID_SQL).subscribe(addressFuture::complete, addressFuture::fail);
            return addressFuture.compose(address -> {
                Future<Integer> resultFuture = Future.future();
                if(Objects.isNull(address) || address.isEmpty()){
                    resultFuture.complete(0);
                    return resultFuture;
                }
                this.execute(new JsonArray().add(addressId).add(address.getLong("versions")), AddressSql.UPDATE_DEFAULT_ADDRESS_SQL)
                        .subscribe(resultFuture::complete, resultFuture::fail);
                return resultFuture;
            });
        }).setHandler(handler);
        return this;
    }
}
