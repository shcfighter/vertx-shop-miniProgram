package com.ecit.shop.handler.impl;

import com.ecit.common.IdBuilder;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.shop.constants.CouponSql;
import com.ecit.shop.handler.ICouponHandler;
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;

import java.util.List;

public class CouponHandler extends JdbcRxRepositoryWrapper implements ICouponHandler {

    public CouponHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public ICouponHandler findCouponList(Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<List<JsonObject>> addressFuture = Future.future();
        this.retrieveMany(new JsonArray().add(System.currentTimeMillis()).add(System.currentTimeMillis()), CouponSql.SELECT_COUPON_SQL).subscribe(addressFuture::complete, addressFuture::fail);
        addressFuture.setHandler(handler);
        return this;
    }

    /**
     * 领取代金券
     * 返回0-代金券不存在；1-领取成功；2-代金券已过期；3-已经领取；
     * @param couponId
     * @param handler
     * @return
     */
    @Override
    public ICouponHandler fetchCoupon(String token, long couponId, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<JsonObject> couponFuture = Future.future();
            this.retrieveOne(new JsonArray().add(couponId), CouponSql.SELECT_COUPON_BY_ID_SQL).subscribe(couponFuture::complete, couponFuture::fail);
            return couponFuture.compose(coupon -> {
               if(JsonUtils.isNull(coupon)){
                   return Future.succeededFuture(0);
               }
               if(coupon.getLong("begin_time") > System.currentTimeMillis() || coupon.getLong("end_time") < System.currentTimeMillis()){
                   return Future.succeededFuture(2);
               }
               Future<JsonObject> detailFuture = Future.future();
               this.retrieveOne(new JsonArray(), CouponSql.SELECT_COUPON_DETAIL_USERID_COUPONID_SQL).subscribe(detailFuture::complete, detailFuture::fail);
               return detailFuture.compose(detail -> {
                    if(detail.getInteger("row_num") > 0){
                        return Future.succeededFuture(0);
                    }
                    Future<UpdateResult> future = Future.future();
                    postgreSQLClient.rxGetConnection().flatMap(conn ->
                            conn.rxSetAutoCommit(false).toSingleDefault(false)
                                    .flatMap(autoCommit -> conn.rxUpdateWithParams(CouponSql.INSERT_COUPON_DETAIL_SQL,
                                            new JsonArray().add(IdBuilder.getUniqueId()).add(coupon.getLong("coupon_id")).add(coupon.getString("coupon_name"))
                                                    .add(coupon.getInteger("coupon_type")).add(coupon.getString("coupon_amount"))
                                                    .add(coupon.getLong("category_id")).add(coupon.getString("category_name")).add(System.currentTimeMillis())
                                                    .add(System.currentTimeMillis() + coupon.getInteger("expiry_date") * 24 * 60 * 60 * 1000)
                                                    .add(userId).add(coupon.getString("min_user_amount")).add(coupon.getInteger("expiry_date"))))
                                    .flatMap(updateResult -> conn.rxUpdateWithParams(CouponSql.UPDATE_COUPON_NUM_SQL,
                                            new JsonArray().add(coupon.getLong("coupon_id"))))
                                    // Rollback if any failed with exception propagation
                                    .onErrorResumeNext(ex -> conn.rxRollback()
                                            .toSingleDefault(true)
                                            .onErrorResumeNext(ex2 -> Single.error(new CompositeException(ex, ex2)))
                                            .flatMap(ignore -> Single.error(ex))
                                    )
                                    // close the connection regardless succeeded or failed
                                    .doAfterTerminate(conn::close)
                    ).subscribe(future::complete, future::fail);
                    return future.compose(result -> {
                       if(result.getUpdated() > 0){
                           return Future.succeededFuture(1);
                       }
                       return Future.succeededFuture(4);
                    });
                });
            });
        }).setHandler(handler);
        return this;
    }
}
