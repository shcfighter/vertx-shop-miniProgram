package com.ecit.shop.handler.impl;

import com.ecit.common.IdBuilder;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.common.utils.WXUtils;
import com.ecit.shop.constants.IntegrationSql;
import com.ecit.shop.constants.UserSql;
import com.ecit.shop.enums.ErrcodeEnum;
import com.ecit.shop.enums.UserStatusEnum;
import com.ecit.shop.handler.IUserHandler;
import com.hazelcast.util.MD5Util;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Created by shwang on 2018/2/2.
 */
public class UserHandler extends JdbcRxRepositoryWrapper implements IUserHandler {

    private static final Logger LOGGER = LogManager.getLogger(UserHandler.class);
    final Vertx vertx;
    final JsonObject config;
    final WebClient webClient;
    public UserHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.config = config;
        webClient = WebClient.create(vertx);
    }

    /**
     * 微信授权
     * @param code
     * @param handler
     * @return
     */
    @Override
    public IUserHandler accredit(String code, JsonObject userInfo, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> future = Future.future();
        webClient.get(443, "api.weixin.qq.com", "/sns/jscode2session")
                .ssl(true)
                .addQueryParam("appid", this.config.getString("weixin.appid"))
                .addQueryParam("secret", this.config.getString("weixin.secret"))
                .addQueryParam("js_code", code)
                .addQueryParam("grant_type", "authorization_code")
                .rxSend()
                .subscribe(success -> future.complete(success.body().toJsonObject()), future::fail);

        future.compose(json -> {
            LOGGER.info("微信授权结果：{}", json);
            if(json.containsKey("errcode")){
                int errcode = json.getInteger("errcode");
                if(errcode != ErrcodeEnum.OK.getKey()){
                    return Future.failedFuture("微信授权失败：" + errcode);
                }
            }
            final String openid = json.getString("openid");
            final String sessionKey = json.getString("session_key");
            Future<JsonObject> userFuture = Future.future();
            this.retrieveOne(new JsonArray().add(openid), UserSql.SELECT_BY_OPENID_SQL)
                    .subscribe(userFuture::complete, userFuture::fail);
            return userFuture.compose(user -> {
                long userId;
                final String token = MD5Util.toMD5String(StringUtils.join(openid, "_", sessionKey, "_", System.currentTimeMillis()));
                JsonObject userSession = new JsonObject().put("open_id", openid).put("session_key", sessionKey).put("token", token);
                if (!JsonUtils.isNull(user)) {
                    userId = user.getLong("user_id");
                    this.executeNoResult(new JsonArray().add(token).add(sessionKey).add(userId).add(user.getLong("versions")), UserSql.UPDATE_USER_TOKEN_SQL).subscribe();
                } else {
                    userId = IdBuilder.getUniqueId();
                    this.execute(new JsonArray().add(userId).add(openid).add(userInfo.getString("nickName")).add(token).add(sessionKey).add(UserStatusEnum.ACTIVATION.getStatus()),
                            UserSql.INSERT_USER_BY_OPENID_SQL).subscribe();
                    this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(userInfo.getString("avatarUrl"))
                            .add(userInfo.getInteger("gender")).add(userInfo.getString("province"))
                            .add(userInfo.getString("city")).add(userInfo.getString("country")), UserSql.INSERT_USER_INFO_SQL).subscribe();
                    this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(5L), IntegrationSql.INSERT_INTEGRATION_SQL).subscribe();
                }
                userSession.put("user_id", userId);
                this.setSession(token, userSession);
                return Future.succeededFuture(new JsonObject().put("token", token).put("uid", userId));
            });
        }).setHandler(handler);
        return this;
    }

    @Override
    public IUserHandler checkToken(String token, Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> future = Future.future();
        this.retrieveOne(new JsonArray().add(token), UserSql.CHECK_USER_TOKEN_SQL)
                .subscribe(result -> future.complete(result.getInteger("num") > 0 ? true : false), future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IUserHandler updateMobile(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            long userId = session.getLong("user_id");
            Future<JsonObject> userFuture = Future.future();
            this.retrieveOne(new JsonArray().add(userId), UserSql.SELECT_USER_SQL).subscribe(userFuture::complete, userFuture::fail);
            return userFuture.compose(user -> {
               if(JsonUtils.isNull(user)){
                   return Future.failedFuture("用户不存在");
               }
               JsonObject mobile = WXUtils.decrypt(params.getString("encryptedData"), session.getString("session_key"), params.getString("iv"));
               System.out.println(mobile);
               Future<Integer> future = Future.future();
               this.execute(new JsonArray().add(mobile.getString("phoneNumber")).add(userId).add(user.getLong("versions")), UserSql.UPDATE_MOBILE_SQL).subscribe(future::complete, future::fail);
               return future;
            });
        }).setHandler(handler);
        return this;
    }

}
