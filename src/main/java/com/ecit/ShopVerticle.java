package com.ecit;

import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.shop.api.ElasticSearchServiceVerticle;
import com.ecit.shop.api.RestShopRxVerticle;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Created by shwang on 2018/2/2.
 */
public class ShopVerticle extends BaseMicroserviceRxVerticle{

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        vertx.getDelegate().deployVerticle(RestShopRxVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
        vertx.getDelegate().deployVerticle(ElasticSearchServiceVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
    }

    public static void main(String[] args) {
        Config cfg = new Config();
        GroupConfig group = new GroupConfig();
        group.setName("p-dev");
        group.setPassword("p-dev");
        cfg.setGroupConfig(group);
        // 申明集群管理器
        ClusterManager mgr = new HazelcastClusterManager(cfg);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.rxClusteredVertx(options).subscribe(v -> v.deployVerticle(ShopVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("instances", 2)
                        .put("user.api.name", "user")
                        .put("account.api.name", "account")
                        .put("postgresql", new JsonObject().put("host", "111.231.132.168")
                                .put("port", 5432)
                                .put("maxPoolSize", 50)
                                .put("username", "postgres")
                                .put("password", "h123456")
                                .put("database", "shop_miniprogram")
                                .put("charset", "UTF-8")
                                .put("queryTimeout", 10000))
                        .put("weixin.appid", "wxf98bdcd6150b7e33")
                        .put("weixin.secret", "35b319c6a9aee16e3977bf0d0d24ff7e")
                        .put("redis", new JsonObject().put("host", "111.231.132.168")
                                .put("port", 6379)
                                .put("password", "h123456")
                        )
                        .put("es", new JsonObject().put("address", "eb.elasticsearch")
                                .put("transportAddresses", new JsonArray().add(new JsonObject()
                                        .put("hostname", "localhost")
                                        .put("port", 9300)))
                                .put("cluster_name", "vertx_shop")
                                .put("client_transport_sniff", false))
                )));
    }
}
