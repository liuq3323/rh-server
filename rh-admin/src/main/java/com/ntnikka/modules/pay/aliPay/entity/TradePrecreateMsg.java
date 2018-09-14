package com.ntnikka.modules.pay.aliPay.entity;

import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;

/**
 * @ClassName TradePrecreateMsg
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/14 11:58
 **/
@TableName("trade_precreate_msg")
public class TradePrecreateMsg implements Serializable {

    private Long orderId;

    private Integer code ;

    private String msg ;



    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
