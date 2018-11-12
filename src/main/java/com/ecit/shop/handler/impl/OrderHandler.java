package com.ecit.shop.handler.impl;

import com.ecit.common.IdBuilder;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.enums.JdbcEnum;
import com.ecit.common.utils.JsonUtils;
import com.ecit.shop.constants.AddressSql;
import com.ecit.shop.constants.CommoditySql;
import com.ecit.shop.constants.CouponSql;
import com.ecit.shop.constants.OrderSql;
import com.ecit.shop.enums.OrderStatus;
import com.ecit.shop.handler.IOrderHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
            List<BigDecimal> totalPrice = new ArrayList<>(1);
            List<BigDecimal> freightPriceList = new ArrayList<>();
            JsonArray orderDetails = new JsonArray();
            return CompositeFuture.all(this.checkCommodity(params.getJsonArray("order_details")))
                .compose(commoditys -> {
                    BigDecimal freightPrice = new BigDecimal(0);
                    List<JsonObject> commodityList = commoditys.list();
                    for (int i = 0; i < commodityList.size(); i++) {
                        JsonObject commodity = commodityList.get(i);
                        if (freightPrice.compareTo(new BigDecimal(commodity.getString("freight_price"))) == -1){
                            freightPrice = new BigDecimal(commodity.getString("freight_price"));
                        }
                        totalPrice.add(new BigDecimal(commodity.getString("price")).multiply(new BigDecimal(commodity.getInteger("buy_num"))));
                        orderDetails.add(new JsonObject().put("commodity_id", commodity.getString("commodity_id")).put("commodity_name", commodity.getString("commodity_name"))
                                .put("num", commodity.getInteger("buy_num")).put("image_url", commodity.getString("image_url"))
                                .put("price", commodity.getString("price")));
                    }
                    totalPrice.add(freightPrice);
                    freightPriceList.add(freightPrice);
                    return commoditys;
                }).compose(commoditys -> {
                    List<JsonObject> commodityList = commoditys.list();
                    List<JsonObject> exec = new ArrayList<>();
                    commodityList.forEach(commodity -> {
                        //减库存
                        if(commodity.containsKey("specifition_name")){
                            //有规格减库存
                            exec.add(new JsonObject().put("type", JdbcEnum.update.name()).put("sql", CommoditySql.UPDATE_BUY_COMMODITY_SPECIFITION_SQL)
                                    .put("params", new JsonArray().add(commodity.getInteger("buy_num")).add(commodity.getString("commodity_id"))
                                            .add(commodity.getString("specifition_name")).add(commodity.getLong("versions"))));
                        } else {
                            //无规格减库存
                            exec.add(new JsonObject().put("type", JdbcEnum.update.name()).put("sql", CommoditySql.UPDATE_BUY_COMMODITY_SQL)
                                    .put("params", new JsonArray().add(commodity.getInteger("buy_num"))
                                            .add(commodity.getString("commodity_id")).add(commodity.getLong("versions"))));
                        }
                    });
                    //查询收货地址
                    Future<JsonObject> addressFuture = Future.future();
                    long addressId = Long.parseLong(params.getString("address_id"));
                    this.retrieveOne(new JsonArray().add(addressId).add(userId), AddressSql.SELECT_ADDRESS_BY_ID_SQL)
                            .subscribe(addressFuture::complete, addressFuture::fail);
                    return addressFuture.compose(address -> {
                       if(JsonUtils.isNull(address)){
                           return Future.failedFuture("收货地址不存在！");
                       }
                       Future<Integer> resultFuture = Future.future();
                       BigDecimal totalPriceDecimal = totalPrice.stream().reduce(BigDecimal::add).orElse(new BigDecimal("0.00"));   //总金额
                       if(params.containsKey("coupon_id")){
                           //有代金券处理
                           Future<JsonObject> couponFuture = Future.future();
                           this.retrieveOne(new JsonArray().add(userId).add(params.getString("coupon_id")), CouponSql.SELECT_COUPON_DETAIL_BY_ID_SQL)
                                   .subscribe(couponFuture::complete, couponFuture::fail);
                           return couponFuture.compose(coupon -> {
                                if(JsonUtils.isNull(coupon)){
                                    return Future.failedFuture("代金券不存在！");
                                }
                                exec.add(new JsonObject().put("type", JdbcEnum.update.name()).put("sql", CouponSql.UPDATE_COUPON_USER_SQL)
                                        .put("params", new JsonArray().add(params.getString("coupon_id")).add(userId)));
                                exec.add(new JsonObject().put("type", JdbcEnum.update.name()).put("sql", OrderSql.INSERT_ORDER_SQL)
                                       .put("params", new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(address.getInteger("country_id"))
                                               .add(address.getInteger("province_id")).add(address.getInteger("city_id"))
                                               .add(address.getInteger("district_id")).add(address.getString("address"))
                                               .add(address.getString("mobile")).add(address.getString("code")).add(orderDetails.encodePrettily())
                                               .add(totalPriceDecimal.toString()).add(OrderStatus.VALID.getValue()).add(params.getString("coupon_id")).add(0)
                                               .add(freightPriceList.get(0).toString()).add(totalPriceDecimal.subtract(freightPriceList.get(0)).toString())));
                                this.executeTransaction(exec).subscribe(re -> resultFuture.complete(re.getUpdated()), resultFuture::fail);
                                return resultFuture;
                           });
                       } else {
                           //无代金券处理
                           exec.add(new JsonObject().put("type", JdbcEnum.update.name()).put("sql", OrderSql.INSERT_ORDER_SQL)
                                   .put("params", new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(address.getInteger("country_id"))
                                           .add(address.getInteger("province_id")).add(address.getInteger("city_id"))
                                           .add(address.getInteger("district_id")).add(address.getString("address"))
                                           .add(address.getString("mobile")).add(address.getString("code")).add(orderDetails.encodePrettily())
                                           .add(totalPriceDecimal.toString()).add(OrderStatus.VALID.getValue()).add(0).add(0)
                                           .add(freightPriceList.get(0).toString()).add(totalPriceDecimal.toString())));
                           this.executeTransaction(exec).subscribe(re -> resultFuture.complete(re.getUpdated()), resultFuture::fail);
                           return resultFuture;
                       }
                    });
                });
        }).setHandler(handler);
        return this;
    }

    /**
     * 检查商品数量是否足够
     * @param orderDetails
     * @return
     */
    private List<Future> checkCommodity(JsonArray orderDetails){
        final List<Future> isOk = new ArrayList<>(orderDetails.size());
        for (int i = 0; i < orderDetails.size(); i++) {
            Future<JsonObject> future = Future.future();
            isOk.add(future);
            JsonObject order = orderDetails.getJsonObject(i);
            String specifitionName = order.getString("specifition_name");
            if(StringUtils.isEmpty(specifitionName)){
                this.retrieveOne(new JsonArray().add(order.getLong("commodity_id")), CommoditySql.SELECT_COMMODITY_SQL)
                        .subscribe(re -> {
                            if(re.getInteger("num") < order.getInteger("number")){
                                Future.failedFuture("商品库存不足");
                                return ;
                            }
                            JsonObject com = re.put("buy_num", order.getInteger("number"));
                            future.complete(com);
                        }, future::fail);
            }else{
                this.retrieveOne(new JsonArray().add(order.getLong("commodity_id")).add(specifitionName), CommoditySql.SELECT_COMMODITY_SPECIFITION_SQL)
                        .subscribe(re -> {
                            if(re.getInteger("num") < order.getInteger("number")){
                                Future.failedFuture("商品库存不足");
                                return ;
                            }
                            JsonObject com = re.put("buy_num", order.getInteger("number"));
                            future.complete(com);
                        }, future::fail);
            }
        }
        return isOk;
    }
}
