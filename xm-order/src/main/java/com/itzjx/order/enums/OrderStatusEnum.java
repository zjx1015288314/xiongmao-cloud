package com.itzjx.order.enums;

public enum OrderStatusEnum {
    UN_PAY(1, "初始化，未付款"),
    PAYED(2, "已付款，未发货"),
    DELIVERED(3, "已发货，未确认"),
    SUCCESS(4, "已确认,未评价"),
    CLOSED(5, "已关闭，交易失败"),
    RATED(6, "已评价，交易结束");

    private int code;
    private String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public int value(){ return this.code;}
}
