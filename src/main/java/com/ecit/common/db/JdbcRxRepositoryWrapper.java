package com.ecit.common.db;

import com.ecit.common.constants.Constants;
import com.ecit.shop.constants.UserSql;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.asyncsql.PostgreSQLClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Helper and wrapper class for JDBC repository services.
 */
public class JdbcRxRepositoryWrapper {

  protected final SQLClient postgreSQLClient;
  protected final RedisClient redisClient;

  public JdbcRxRepositoryWrapper(Vertx vertx, JsonObject config) {
    this.postgreSQLClient = PostgreSQLClient.createShared(vertx, config);
    JsonObject redis = config.getJsonObject("redis");
    this.redisClient = RedisClient.create(vertx, new RedisOptions().setHost(redis.getString("host", "localhost"))
            .setPort(redis.getInteger("port", 6379)).setAuth(redis.getString("password")));
  }

  /**
   * Suitable for `add`, `exists` operation.
   *
   * @param params        query params
   * @param sql           sql
   */
  protected Single<Integer> executeNoResult(JsonArray params, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxUpdateWithParams(sql, params)
                    .map(UpdateResult::getUpdated).doAfterTerminate(conn::close));
  }

  protected Single<Integer> execute(JsonArray params, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxUpdateWithParams(sql, params)
                    .map(UpdateResult::getUpdated).doAfterTerminate(conn::close));
  }

  protected Single<JsonObject> retrieveOne(JsonArray param, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxQueryWithParams(sql, param).map(rs -> {
              List<JsonObject> resList = rs.getRows();
              if (resList == null || resList.isEmpty()) {
                return new JsonObject();
              } else {
                return resList.get(0);
              }
            }).doAfterTerminate(conn::close));

  }

  protected int calcPage(int page, int size) {
    if (page <= 0)
      return 0;
    return size * (page - 1);
  }

  protected Single<List<JsonObject>> retrieveByPage(JsonArray param, int size, int page, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxQueryWithParams(sql, param.add(size).add(calcPage(page, size)))
                    .map(ResultSet::getRows).doAfterTerminate(conn::close));
  }

  protected Single<List<JsonObject>> retrieveMany(JsonArray param, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxQueryWithParams(sql, param)
                    .map(ResultSet::getRows).doAfterTerminate(conn::close));
  }

  protected Single<List<JsonObject>> retrieveAll(String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxQuery(sql)
                    .map(ResultSet::getRows).doAfterTerminate(conn::close));
  }

  protected Single<Integer> removeOne(Object id, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxUpdateWithParams(sql, new JsonArray().add(id))
                    .map(UpdateResult::getUpdated).doAfterTerminate(conn::close));
  }

  protected Single<Integer> removeAll(String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxUpdate(sql).map(UpdateResult::getUpdated)
                    .doAfterTerminate(conn::close));
  }

  protected Single<SQLConnection> getConnection() {
    return postgreSQLClient.rxGetConnection();
  }

    /**
     * 缓存获取token
     * @param token
     * @return
     */
  protected Future<JsonObject> getSession(String token){
      if(StringUtils.isEmpty(token)){
          return Future.succeededFuture(new JsonObject());
      }
      Future<String> redisResult = Future.future();
      redisClient.rxHget(Constants.VERTX_WEB_SESSION, token).subscribe(redisResult::complete, redisResult::fail);
      return redisResult.compose(user -> {
          System.out.println("u: " + user);
          if(StringUtils.isEmpty(user)){
              Future<JsonObject> future = Future.future();
              this.retrieveOne(new JsonArray().add(token), UserSql.SELECT_BY_TOKEN_SQL)
                      .subscribe(future::complete, future::fail);
              future.compose(u ->{
                  this.setSession(token, u);
                  return Future.succeededFuture();
              });
              return future;
          }
          return Future.succeededFuture(new JsonObject(user));
      });
  }

    /**
     * token添加缓存
     * @param token
     * @param jsonObject
     */
  protected void setSession(String token, JsonObject jsonObject){
      redisClient.rxHset(Constants.VERTX_WEB_SESSION, token, jsonObject.toString()).subscribe();
      redisClient.rxExpire(Constants.VERTX_WEB_SESSION, Constants.SESSION_EXPIRE_TIME).subscribe();
  }
}
