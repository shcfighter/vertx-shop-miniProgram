package com.ecit.shop.handler.impl;

import com.ecit.common.IdBuilder;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.shop.constants.OrderSql;
import com.ecit.shop.enums.OrderStatus;
import com.ecit.shop.handler.IOrderHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderHandler extends JdbcRxRepositoryWrapper implements IOrderHandler {
    private static final Logger LOGGER = LogManager.getLogger(OrderHandler.class);

    public OrderHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public IOrderHandler insertOrder(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<Integer> resultFuture = Future.future();
            this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(params.getInteger("country_id")).add(params.getInteger("province_id"))
                    .add(params.getInteger("city_id")).add(params.getInteger("district_id")).add(params.getString("address"))
                    .add(params.getString("mobile")).add(params.getString("postcode")).add(params.getString("order_details"))
                    .add(params.getString("total_price")).add(OrderStatus.VALID.getValue()).add(params.getString("coupon_id")).add(0)
                    .add(params.getString("freight_price"))
                    , OrderSql.INSERT_ORDER_SQL).subscribe(resultFuture::complete, resultFuture::fail);
            return resultFuture;
        }).setHandler(handler);
        return this;
    }
}
