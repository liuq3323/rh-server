package com.ntnikka.modules.pay.aliPay.entity;

import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by liuq on 2018/9/11.
 */

@TableName("trade_order")
public class AliOrderEntity implements Serializable {


    private static final long serialVersionUID = 1L;


    private String sysTradeNo;

    /*
    接口类型
     */
    private String payType;
    /*
    接口版本
     */
    private String version;
    /*
    签名方式（MD5）
     */
    private String signType;
    /*
    商户订单号
     */
    private String orderId;
    /*
    商户订单金额(金额最小0.01)
     */
    private BigDecimal orderAmount;
    /*
    商户号（签约的商户对应的本平台唯一用户号）
     */
    private String partner;
    /*
    签名
     */
    private String sign;
    /*
    回调地址
     */
    private String notifyUrl;
    /*
    商品代码
     */
    private Integer productId;
    /*
    商品名称
     */
    private String productName;
    /*
    商品描述
     */
    private String productDesc;

    private String payMethod;

    private Date createTime;

    private Date updateTime;

    private Long merchantId;

    private String tradeNo;

    private Date payTime;

    private String merchantName;

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getSysTradeNo() {
        return sysTradeNo;
    }

    public void setSysTradeNo(String sysTradeNo) {
        this.sysTradeNo = sysTradeNo;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }


    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public static boolean checkParam(AliOrderEntity aliOrderEntity) {
        if (null == aliOrderEntity.getNotifyUrl() || null == aliOrderEntity.getOrderAmount() || null == aliOrderEntity.getOrderId()
                || null == aliOrderEntity.getMerchantId() || null == aliOrderEntity.getPayType() || null == aliOrderEntity.getVersion()
                || null == aliOrderEntity.getSign() || null == aliOrderEntity.getSignType() || null == aliOrderEntity.getProductName()
                || null == aliOrderEntity.getPayMethod() || "".equals(aliOrderEntity.getPayMethod()))
            return false;
        return true;
    }
}
