package com.ecit.shop.enums;

public enum BannerShowEnum {
    /**
     * 0=未激活
     */
    INACTIVATED(0, "未激活"),
    /**
     *1=激活
     */
    ACTIVATION(1, "激活"),
    /**
     * 2=禁用
     */
    DISABLED(2, "禁用"),
    /**
     *-1=删除
     */
    DELETED(-1, "删除");

    private int key;
    private String value;

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    BannerShowEnum() {
    }

    BannerShowEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
