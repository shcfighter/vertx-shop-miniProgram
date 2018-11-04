package com.ecit.shop.api;

import com.ecit.auth.ShopUserSessionHandler;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.shop.handler.IAddressHandler;
import com.ecit.shop.handler.IBannerHandler;
import com.ecit.shop.handler.ICommodityHandler;
import com.ecit.shop.handler.IUserHandler;
import com.ecit.shop.handler.impl.AddressHandler;
import com.ecit.shop.handler.impl.BannerHandler;
import com.ecit.shop.handler.impl.CommodityHandler;
import com.ecit.shop.handler.impl.UserHandler;
import com.hubrick.vertx.elasticsearch.model.Hits;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by shwang on 2018/2/2.
 */
public class RestShopRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestShopRxVerticle.class);
    private IUserHandler userHandler;
    private IAddressHandler addressHandler;
    private IBannerHandler bannerHandler;
    private ICommodityHandler commodityHandler;

    @Override
    public void start() throws Exception {
        super.start();

        this.userHandler = new UserHandler(vertx, this.config());
        this.addressHandler = new AddressHandler(vertx, this.config());
        this.bannerHandler = new BannerHandler(vertx, this.config());
        this.commodityHandler = new CommodityHandler(vertx, this.config());
        final Router router = Router.router(vertx);
        // cookie and session handler
        this.enableLocalSession(router, "shop_session");
        this.enableCorsSupport(router);
        // body handler
        router.route().handler(BodyHandler.create());
        //不需要登录
        router.get("/api/accredit").handler(this::accreditHandler);     //微信授权
        router.get("/api/user/check").handler(this::checkTokenHandler);     //检查校验token

        router.get("/api/banner").handler(this::bannerHandler);       //获取banner信息
        router.get("/api/category").handler(this::categoryHandler);     //获取category信息
        router.post("/api/commodity/search").handler(this::searchHandler);     //搜索商品信息
        router.get("/api/commodity/detail/:id").handler(this::findCommodityFromESByIdHandler);     //搜索商品信息

        router.getDelegate().route().handler(ShopUserSessionHandler.create(vertx.getDelegate(), this.config()));

        // API route handler    需要登录
        /**
         * 收货地址
         */
        router.post("/api/insertAddress").handler(this::insertAddressHandler);      //新增收货地址
        router.put("/api/updateAddress").handler(this::updateAddressHandler);      //修改收货地址
        router.get("/api/addressList").handler(this::addressListHandler);        //收货地址列表
        router.get("/api/addressDetail/:id").handler(this::addressDetailHandler);       //收货地址详情
        router.delete("/api/delAddress/:id").handler(this::addressDelHandler);      //删除收货地址




        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("user.http.address", "localhost");
        int port = config().getInteger("user.http.port", 8080);

        // create HTTP server and publish REST handler
        createHttpServer(router, host, port).subscribe(server -> {
            LOGGER.info("shop-user server started!");
        }, error -> {
            LOGGER.info("shop-user server start fail!", error);
        });
    }

    /**
     * 授权登录
     * @param context
     */
    private void accreditHandler(RoutingContext context){
        userHandler.accredit(context.request().getParam("code"), handler -> {
            if(handler.failed()){
                LOGGER.info("授权结果：", handler.cause());
                this.returnWithFailureMessage(context, "授权失败");
                return ;
            }
            JsonObject result = handler.result();
            this.returnWithSuccessMessage(context, "授权成功", result);
            return ;
        });
    }

    /**
     * 检查token是否正确
     * @param context
     */
    private void checkTokenHandler(RoutingContext context){
        final String token = context.request().getHeader("token");
        if(StringUtils.isEmpty(token)){
            LOGGER.info("检查token为空");
            this.returnWithFailureMessage(context, "授权失败");
            return;
        }
        userHandler.checkToken(token, handler -> {
            if(handler.failed() || !handler.result()){
                LOGGER.info("token【{}】授权失败", token, handler.cause());
                this.returnWithFailureMessage(context, "授权失败");
                return;
            }
            this.returnWithSuccessMessage(context, "授权成功");
            return;
        });
    }

    /**
     * 新增收货地址
     * @param context
     */
    private void insertAddressHandler(RoutingContext context){
        JsonObject params = context.getBodyAsJson();
        addressHandler.insertAddress(context.request().getHeader("token"), params, hander -> {
            if(hander.failed() || hander.result() <= 0){
                LOGGER.info("新增地址失败：", hander.cause());
                this.returnWithFailureMessage(context, "新增收货地址失败");
                return;
            }
            this.returnWithSuccessMessage(context, "新增收货地址成功");
            return ;
        });
    }

    /**
     * 修改收货地址
     * @param context
     */
    private void updateAddressHandler(RoutingContext context){
        JsonObject params = context.getBodyAsJson();
        addressHandler.updateAddress(context.request().getHeader("token"), params, hander -> {
            if(hander.failed() || hander.result() <= 0){
                LOGGER.info("修改地址失败：", hander.cause());
                this.returnWithFailureMessage(context, "修改收货地址失败");
                return;
            }
            this.returnWithSuccessMessage(context, "修改收货地址成功");
            return ;
        });
    }

    /**
     * 收货地址列表
     * @param context 上下文
     */
    private void addressListHandler(RoutingContext context){
        addressHandler.listAddress(context.request().getHeader("token"), hander -> {
            if(hander.failed()){
                LOGGER.info("获取收货地址失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取收货地址失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取收货地址成功", hander.result());
            return;
        });
    }

    /**
     * 查询收货地址详情
     * @param context
     */
    private void addressDetailHandler(RoutingContext context){
        addressHandler.getAddressById(context.request().getHeader("token"), Long.parseLong(context.pathParam("id")), hander -> {
            if(hander.failed()){
                LOGGER.info("获取收货地址失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取收货地址失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取收货地址成功", hander.result());
            return;
        });
    }

    /**
     * 删除收货地址
     * @param context
     */
    private void addressDelHandler(RoutingContext context){
        addressHandler.delAddress(context.request().getHeader("token"), Long.parseLong(context.pathParam("id")), hander -> {
            if(hander.failed()){
                LOGGER.info("删除收货地址失败:", hander.cause());
                this.returnWithFailureMessage(context, "删除收货地址失败");
                return;
            }
            this.returnWithSuccessMessage(context, "删除收货地址成功", hander.result());
            return;
        });
    }

    /**
     * 获取banner信息
     * @param context
     */
    private void bannerHandler(RoutingContext context){
        bannerHandler.banner(hander -> {
            if(hander.failed()){
                LOGGER.info("获取banner信息失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取banner信息失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取banner信息成功", hander.result());
            return;
        });
    }

    /**
     * 获取category信息
     */
    private void categoryHandler(RoutingContext context){
        commodityHandler.category(hander -> {
            if(hander.failed()){
                LOGGER.info("获取商品类别信息失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取商品类别信息失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取商品类别信息成功", hander.result());
            return;
        });
    }

    /**
     * 搜索商品信息
     * @param context
     */
    private void searchHandler(RoutingContext context){
        final JsonObject params = context.getBodyAsJson();
        final String keyword = params.getString("keyword");
        final String category = params.getString("category");
        final int page = Optional.ofNullable(params.getInteger("page")).orElse(1);
        long start = System.currentTimeMillis();
        commodityHandler.searchCommodity(keyword, category, Optional.ofNullable(params.getInteger("pageSize")).orElse(12), page, handler -> {
            LOGGER.info("查询商品结束线程：{}, search time:{}", Thread.currentThread().getName(), System.currentTimeMillis() - start);
            if(handler.failed()){
                LOGGER.info("搜索商品异常：", handler.cause());
                this.returnWithFailureMessage(context, "暂无该商品！");
                return ;
            }
            if(Objects.isNull(handler.result())){
                this.returnWithFailureMessage(context, "暂无该商品！");
                return ;
            }
            final SearchResponse result = handler.result();
            this.returnWithSuccessMessage(context, "查询成功", result.getHits().getTotal().intValue(),
                    result.getHits().getHits().stream().map(hit -> hit.getSource()).collect(Collectors.toList()), page);
            return ;
        });
    }

    /**
     * 根据商品id查询详情信息(数据源：elasticsearch)
     * @param context
     */
    private void findCommodityFromESByIdHandler(RoutingContext context){
        System.out.println(Long.parseLong(context.request().getParam("id")));
        commodityHandler.findCommodityFromEsById(Long.parseLong(context.request().getParam("id")), handler -> {
            if (handler.failed()) {
                LOGGER.info("根据id查询商品失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询商品失败");
                return ;
            } else {
                System.out.println(handler.result());
                final Hits hits = handler.result().getHits();
                JsonObject result = new JsonObject().put("basicInfo", hits.getHits().get(0).getSource());
                this.returnWithSuccessMessage(context, "", result);
                return ;
            }
        });
    }
}
