package com.ntnikka.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.ntnikka.modules.pay.aliPay.config.AlipayConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AliPayRequest {
    public static String  doQrCodeAliRequest(Long orderId ,Double orderAmount , String productName)throws AlipayApiException{
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", AlipayConfig.app_id, AlipayConfig.private_key, "json", AlipayConfig.input_charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type_RSA2); //获得初始化的AlipayClient
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();//创建API对应的request类
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+orderId+"\"," +
                "    \"total_amount\":\""+orderAmount+"\"," +
                "    \"subject\":\""+productName+"\"," +
                "    \"timeout_express\":\"1c\"}");//该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m
        //设置业务参数
        AlipayTradePrecreateResponse response = alipayClient.execute(request);
        System.out.print(response.getBody());
        //根据response中的结果继续业务逻辑处理
        return response.getBody();
    }

    public static void main(String[] args) {
        String jsonStr = "{\"alipay_trade_precreate_response\":{\"code\":\"10000\",\"msg\":\"Success\",\"out_trade_no\":\"2015032001010100211\",\"qr_code\":\"https:\\/\\/qr.alipay.com\\/bax043173wzfnxohhdho6090\"},\"sign\":\"OUt2HdOZllr6w9txa45KSXy1fGnEhUPKOv7149nasPZdjojseYWDDQjc6pNLy2nJn9Uh4lahbGoPORtwRo26MlUWlMv+jcHGWmKbIvo2LNEYBaRSsi+nBPs3W7VF/D5MTC4pfwEuTK+dZBYuPCSuonWX4baLOVrBeFq4/k+t1UsheQom3n9ps0Xgxc17YQ8DQcMgzt+6W5zJgHqp8CkFPpKZjxf+P1UrbWgqHhNKhMg6gEscXk14dY6B90eta95PKUCWhlmUn6yn/KU4QqJF1BLkVHKHx/cq6kpIuckMeojsav0uQQT/vUfQcsaxX+8mhDR+OEPCLn9ocTpmzJeiCw==\"}";
        JSONObject json = JSON.parseObject(jsonStr).getJSONObject("alipay_trade_precreate_response");
        System.out.println(json);
    }
}
