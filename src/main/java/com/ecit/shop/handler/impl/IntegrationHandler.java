package com.ecit.shop.handler.impl;

import com.ecit.common.constants.Constants;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.shop.constants.IntegrationSql;
import com.ecit.shop.handler.IIntegrationHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

public class IntegrationHandler extends JdbcRxRepositoryWrapper implements IIntegrationHandler {

    public IntegrationHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public IIntegrationHandler integrationRowNum(String token, Handler<AsyncResult<Long>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<Long> future = Future.future();
            this.retrieveOne(new JsonArray().add(userId), IntegrationSql.SELECT_INTEGRATION_SQL)
                    .subscribe(re -> {
                        future.complete(StringUtils.isNotEmpty(re.getString("row_num")) ? Long.parseLong(re.getString("row_num")) : 0L);
                    }, future::fail);
            return future;
        }).setHandler(handler);
        return this;
    }

    @Override
    public IIntegrationHandler signIn(String token, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<JsonObject> integrationFuture = Future.future();
            this.retrieveOne(new JsonArray().add(userId), IntegrationSql.SELECT_INTEGRATION_BY_USER_SQL)
                    .subscribe(integrationFuture::complete, integrationFuture::fail);
            return integrationFuture.compose(integration -> {
                if(JsonUtils.isNull(integration)){
                    return Future.failedFuture("签到失败");
                }
                Future<String> signFuture = Future.future();
                redisClient.rxGet(Constants.SIGN_IN_KEY + userId).subscribe(signFuture::complete, signFuture::fail);
                return signFuture.compose(sign -> {
                    if(StringUtils.isNotEmpty(sign)){
                        return Future.succeededFuture(-1);
                    }
                    Future<Integer> updateFuture = Future.future();
                    this.execute(new JsonArray().add(1).add(userId).add(integration.getLong("versions")), IntegrationSql.UPDATE_INTEGRATION_SQL)
                            .subscribe(re -> {
                                redisClient.rxSet(Constants.SIGN_IN_KEY + userId, "sign").subscribe();
                                redisClient.rxExpire(Constants.SIGN_IN_KEY + userId, this.getSecondsNextEarlyMorning()).subscribe();
                        updateFuture.complete(re);
                    }, updateFuture::fail);
                    return updateFuture;
                });
            });
        }).setHandler(handler);
        return this;
    }

    /**
     * 获取当前时间到第二天凌晨的秒数
     * @return
     */
    private Long getSecondsNextEarlyMorning() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
    }
}
