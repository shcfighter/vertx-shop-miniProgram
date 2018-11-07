package com.ecit.shop.enums;

/**
 * Created by shwang on 2018/2/5.
 */
public enum CommodityStatusEnum {
    /**
     * 0=草稿
     */
    INACTIVATED(0, "草稿"),
    /**
     *1=上架
     */
    ACTIVATION(1, "上架"),
    /**
     * 2=下架
     */
    DISABLED(2, "下架");

    private int status;
    private String desc;

    CommodityStatusEnum() {
    }

    CommodityStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
