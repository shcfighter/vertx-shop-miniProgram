package com.ecit.shop.enums;

/**
 * 订单状态
 */
public enum OrderStatusEnum {
    /**
     * 1=有效
     */
    VALID(1, "有效"),
    /**
     * 2=已付款
     */
    PAY(2, "已付款"),
    /**
     * 3=已发货
     */
    SHIP(3, "已发货"),
    /**
     * 4=订单完成
     */
    FINISHED(4, "订单完成"),
    /**
     * 5=评价
     */
    EVALUATION(5, "评价"),
    /**
     * 6=已退款
     */
    REFUND(6, "已退款"),
    /**
     * 7=申请退款退货
     */
    AUDIT_REFUND(7, "申请退款退货"),
    /**
     * -1=已取消订单
     */
    CANCEL(-1, "已取消订单");

    private int value;
    private String desc;

    OrderStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
