package com.ntnikka.modules.pay.aliPay.entity;

import java.io.Serializable;

/**
 * @ClassName TradeQueryParam
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/25 10:09
 **/
public class TradeQueryParam implements Serializable {
    private Long orderId;

    private String sign;

    private String partner;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }
}
