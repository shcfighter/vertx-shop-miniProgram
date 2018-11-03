package com.ecit.shop.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.shop.constants.CategorySql;
import com.ecit.shop.handler.ICommodityHandler;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by shwang on 2018/2/2.
 */
public class CommodityHandler extends JdbcRxRepositoryWrapper implements ICommodityHandler {

    private static final Logger LOGGER = LogManager.getLogger(CommodityHandler.class);
    /**
     * es商品索引indeces
     */
    private static final String SHOP_INDICES = "shop";

    final RxElasticSearchService rxElasticSearchService;

    final Vertx vertx;
    final JsonObject config;
    public CommodityHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.config = config;
        this.rxElasticSearchService = RxElasticSearchService.createEventBusProxy(this.vertx.getDelegate(), config.getString("address"));
    }


    @Override
    public ICommodityHandler category(Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<List<JsonObject>> future = Future.future();
        this.retrieveAll(CategorySql.SELECT_CATEGORY_SQL).subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public ICommodityHandler searchCommodity(String keyword, String category, int pageSize, int page, Handler<AsyncResult<SearchResponse>> handler) {
        Future<SearchResponse> future = Future.future();
        JsonObject searchJson = null;
        if(StringUtils.equals("全部", category)){
            category = null;
        }
        if (StringUtils.isBlank(keyword) && StringUtils.isBlank(category)) {
            searchJson = new JsonObject("{\"match_all\": {}}");
        } else {
            searchJson = new JsonObject("{\n" +
                    "    \"bool\": {\n" +
                    "      \"should\": [\n" +
                    "        {\n" +
                    "          \"multi_match\" : {\n" +
                    "            \"query\":      \"" + keyword + "\",\n" +
                    "            \"fields\":     [ \"commodity_name\", \"brand_name\", \"category_name\", \"remarks\", \"description\", \"large_class\" ]\n" +
                    "          }\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"match\" : {\n" +
                    "            \"category_name\":      \"" + category + "\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  }");
        }
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(searchJson)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true).setSize(pageSize).setFrom(this.calcPage(page, pageSize))
                //.addFieldSort("commodity_id", SortOrder.DESC);
                .addScripSort("Math.random()", ScriptSortOption.Type.NUMBER, new JsonObject(), SortOrder.DESC); //随机排序
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
