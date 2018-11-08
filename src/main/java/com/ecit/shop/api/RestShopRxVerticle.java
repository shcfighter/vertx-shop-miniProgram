package com.ecit.shop.api;

import com.ecit.auth.ShopUserSessionHandler;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.shop.handler.*;
import com.ecit.shop.handler.impl.*;
import com.hubrick.vertx.elasticsearch.model.Hits;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import io.vertx.core.json.JsonArray;
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
    private ICartHandler cartHandler;
    private ICouponHandler couponHandler;

    @Override
    public void start() throws Exception {
        super.start();

        this.userHandler = new UserHandler(vertx, this.config());
        this.addressHandler = new AddressHandler(vertx, this.config());
        this.bannerHandler = new BannerHandler(vertx, this.config());
        this.commodityHandler = new CommodityHandler(vertx, this.config());
        this.cartHandler = new CartHandler(vertx, this.config());
        this.couponHandler = new CouponHandler(vertx, this.config());

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
        router.get("/api/commodity/detail/:id").handler(this::findCommodityFromESByIdHandler);     //搜索商品详情信息
        router.post("/api/commodity/specifition/price/:id").handler(this::findCommoditySpecifitionPriceHandler);     //查询商品价格
        router.post("/api/commodity/price/:id").handler(this::findCommodityPriceHandler);     //查询商品价格
        router.get("/api/coupons").handler(this::couponHandler);     //代金券信息列表

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
        router.get("/api/defaultAddress").handler(this::defaultAddressHandler);       //默认收货地址详情
        router.put("/api/updateDefaultAddress/:id").handler(this::updateDefaultAddressHandler);       //修改默认收货地址详情
        /**
         * 购物车
         */
        router.post("/api/insertCart").handler(this::insertCartHandler);      //新增购物车
        router.get("/api/cartList").handler(this::cartListHandler);        //购物车列表
        router.get("/api/findCart").handler(this::findCartHandler);        //购物车列表
        router.delete("/api/delCart/:id").handler(this::cartDelHandler);      //删除收货地址
        router.delete("/api/delBatchCart").handler(this::cartDelBatchHandler);      //批量删除收货地址
        /**
         * 代金券
         */
        router.put("/api/coupon/fetch/:id").handler(this::fetchCouponHandler);     //代金券信息列表
        router.get("/api/findCoupon").handler(this::findCouponHandler);     //可用代金券信息列表


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
     * 获取默认收货地址
     * @param context
     */
    private void defaultAddressHandler(RoutingContext context){
        addressHandler.findDefaultAddress(context.request().getHeader("token"), hander -> {
            if(hander.failed()){
                LOGGER.info("获取默认收货地址失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取默认收货地址失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取默认收货地址成功", hander.result());
            return;
        });
    }

    /**
     * 修改默认收货地址
     * @param context
     */
    private void updateDefaultAddressHandler(RoutingContext context){
        addressHandler.updateDefaultAddress(context.request().getHeader("token"), Long.parseLong(context.pathParam("id")), hander -> {
            if(hander.failed()){
                LOGGER.info("修改默认收货地址失败:", hander.cause());
                this.returnWithFailureMessage(context, "修改默认收货地址失败");
                return;
            }
            this.returnWithSuccessMessage(context, "修改默认收货地址成功", hander.result());
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
        commodityHandler.findCommodityFromEsById(Long.parseLong(context.request().getParam("id")), handler -> {
            if (handler.failed()) {
                LOGGER.info("根据id查询商品失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询商品失败");
                return ;
            } else {
                final Hits hits = handler.result().getHits();
                JsonObject items = hits.getHits().get(0).getSource();
                JsonArray images = items.getJsonArray("detail_image_url");
                JsonArray commodityParams = items.getJsonArray("commodity_params");
                StringBuilder content = new StringBuilder();
                if(Objects.nonNull(commodityParams) && !commodityParams.isEmpty()){
                    commodityParams.forEach(param -> {
                        content.append("<p>");
                        content.append(param);
                        content.append("</p>");
                    });
                }
                images.forEach(image -> {
                    content.append("<p><img src=\"");
                    content.append(image);
                    content.append("_m");
                    content.append("\" style=\"\" title=\"");
                    content.append(image);
                    content.append("\"/></p>");
                });

                JsonObject result = new JsonObject().put("basicInfo", hits.getHits().get(0).getSource())
                        .put("content", content.toString());
                this.returnWithSuccessMessage(context, "查询商品信息详情成功", result);
                return ;
            }
        });
    }

    /**
     *  获取商品价格
     * @param context
     */
    private void findCommoditySpecifitionPriceHandler(RoutingContext context){
        JsonObject params = context.getBodyAsJson();
        commodityHandler.findCommoditySpecifitionPrice(Long.parseLong(context.pathParam("id")), params.getString("specifition_name"),
                hander -> {
            if(hander.failed()){
                LOGGER.info("获取商品价格信息失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取商品价格信息失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取商品价格信息成功", hander.result());
            return;
        });
    }

    /**
     *  获取商品价格
     * @param context
     */
    private void findCommodityPriceHandler(RoutingContext context){
        commodityHandler.findCommodityPrice(Long.parseLong(context.pathParam("id")), hander -> {
            if(hander.failed()){
                LOGGER.info("获取商品价格信息失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取商品价格信息失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取商品价格信息成功", hander.result());
            return;
        });
    }

    /**
     * 新增购物车
     * @param context
     */
    private void insertCartHandler(RoutingContext context){
        JsonObject params = context.getBodyAsJson();
        cartHandler.insertCart(context.request().getHeader("token"), params, hander -> {
            if(hander.failed() || hander.result() <= 0){
                LOGGER.info("新增购物车失败：", hander.cause());
                this.returnWithFailureMessage(context, "新增购物车失败");
                return;
            }
            this.returnWithSuccessMessage(context, "新增购物车成功");
            return ;
        });
    }

    /**
     * 购物车列表
     * @param context 上下文
     */
    private void cartListHandler(RoutingContext context){
        cartHandler.cartList(context.request().getHeader("token"), hander -> {
            if(hander.failed()){
                LOGGER.info("获取购物车失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取购物车失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取购物车成功", hander.result());
            return;
        });
    }

    /**
     * 查询商品数量
     * @param context
     */
    private void findCartHandler(RoutingContext context){
        cartHandler.findCart(context.request().getHeader("token"), context.getBodyAsJson(), hander -> {
            if(hander.failed()){
                LOGGER.info("获取购物车失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取购物车失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取购物车成功", hander.result());
            return;
        });
    }

    /**
     * 删除购物车商品
     * @param context
     */
    private void cartDelHandler(RoutingContext context){
        cartHandler.delCart(context.request().getHeader("token"), Long.parseLong(context.pathParam("id")), context.getBodyAsJson(), hander -> {
            if(hander.failed()){
                LOGGER.info("删除购物车商品失败:", hander.cause());
                this.returnWithFailureMessage(context, "删除购物车商品失败");
                return;
            }
            this.returnWithSuccessMessage(context, "删除购物车商品成功", hander.result());
            return;
        });
    }
    /**
     * 批量删除购物车商品
     * @param context
     */
    private void cartDelBatchHandler(RoutingContext context){
        cartHandler.delBatchCart(context.request().getHeader("token"), context.getBodyAsJson(), hander -> {
            if(hander.failed()){
                LOGGER.info("批量删除购物车商品失败:", hander.cause());
                this.returnWithFailureMessage(context, "批量删除购物车商品失败");
                return;
            }
            this.returnWithSuccessMessage(context, "批量删除购物车商品成功", hander.result());
            return;
        });
    }

    /**
     * 获取代金券信息列表
     * @param context
     */
    private void couponHandler(RoutingContext context){
        couponHandler.findCouponList(hander -> {
            if(hander.failed()){
                LOGGER.info("获取代金券信息列表失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取代金券信息列表失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取代金券信息列表成功", hander.result());
            return;
        });
    }

    /**
     * 领取代金券
     * @param context
     */
    private void fetchCouponHandler(RoutingContext context){
        couponHandler.fetchCoupon(context.request().getHeader("token"), Long.parseLong(context.pathParam("id")),
                hander -> {
            if(hander.failed()){
                LOGGER.info("领取代金券失败:", hander.cause());
                this.returnWithFailureMessage(context, "领取代金券失败");
                return;
            }
            this.returnWithSuccessMessage(context, "领取代金券成功", hander.result());
            return;
        });
    }

    /**
     * 可用代金券列表
     * @param context
     */
        private void q(RoutingContext context){
        couponHandler.findCoupon(context.request().getHeader("token"), hander -> {
            if(hander.failed()){
                LOGGER.info("获取可用代金券列表失败:", hander.cause());
                this.returnWithFailureMessage(context, "获取可用代金券列表失败");
                return;
            }
            this.returnWithSuccessMessage(context, "获取可用代金券列表成功", hander.result());
            return;
        });
    }

}
