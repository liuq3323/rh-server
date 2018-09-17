package com.ntnikka.modules.pay.aliPay.contorller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ntnikka.common.Enum.PayTypeEnum;
import com.ntnikka.common.utils.AliPayRequest;
import com.ntnikka.modules.pay.aliPay.entity.AliOrderEntity;
import com.ntnikka.modules.pay.aliPay.entity.TradePrecreateMsg;
import com.ntnikka.modules.pay.aliPay.service.AliOrderService;
import com.ntnikka.modules.pay.aliPay.service.TradePrecreateMsgService;
import com.ntnikka.modules.pay.aliPay.utils.ImageToBase64Util;
import com.ntnikka.modules.pay.aliPay.utils.SignUtil;
import com.ntnikka.modules.sys.controller.AbstractController;
import com.ntnikka.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Created by liuq on 2018/9/11.
 */

@RestController
@RequestMapping("/api/v1")
public class AliPayController extends AbstractController {

    @Autowired
    private AliOrderService aliOrderService;

    @Autowired
    private TradePrecreateMsgService tradePrecreateMsgService;

    @RequestMapping(value = "/test" , method = RequestMethod.POST)
    public R testController(@RequestBody AliOrderEntity aliOrderEntity){
        //System.out.println("aliOrderEntity = [" + aliOrderEntity + "]");
        //1.校验必填参数
        if (!AliOrderEntity.checkParam(aliOrderEntity)){
            return R.error(403000 , "缺少参数");
        }
        //2.校验签名
        String sign = aliOrderEntity.getSign();
        String ParamStr = String.format("orderAmount=%s&orderId=%s&partner=%s&payMethod=%s&payType=%s&signType=%s&version=%s",aliOrderEntity.getOrderAmount(),
                aliOrderEntity.getOrderId(),aliOrderEntity.getPartner(),aliOrderEntity.getPayMethod(),aliOrderEntity.getPayType(),aliOrderEntity.getSignType(),
                aliOrderEntity.getVersion());
        if (!SignUtil.checkSign(sign , ParamStr)){
            return R.error(403013 , "验签失败");
        }
        int count = aliOrderService.checkRepeatId(aliOrderEntity.getOrderId());
        if (count > 0){
            return R.error(403014, "订单流水号重复");
        }
        //3.订单信息入库
        aliOrderEntity.setCreateTime(new Date());
        aliOrderEntity.setUpdateTime(new Date());
        aliOrderService.save(aliOrderEntity);
        try {
            String result = AliPayRequest.doQrCodeAliRequest(aliOrderEntity.getOrderId() ,aliOrderEntity.getOrderAmount() ,aliOrderEntity.getProductName());
            JSONObject resultJson = JSON.parseObject(result).getJSONObject("alipay_trade_precreate_response");
            // TODO: 2018/9/14  请求支付宝预下单接口 最好异步保存返回信息
            runAsync(() -> {//异步保存
                TradePrecreateMsg tradePrecreateMsg = new TradePrecreateMsg();
                tradePrecreateMsg.setCode(resultJson.getInteger("code"));
                tradePrecreateMsg.setOrderId(aliOrderEntity.getOrderId());
                tradePrecreateMsg.setMsg(resultJson.getString("msg"));
            });
            if (resultJson.getInteger("code") != 10000){
                return R.error().put("sub_code",resultJson.getString("sub_code")).put("sub_msg",resultJson.getString("sub_msg"));
            }
            //4.调起支付宝下单接口 根据不同的payType处理不同下单方式
            //wap支付
            //直接返回支付宝返回的二维码url
            Map resultMap = new HashMap();
            if (PayTypeEnum.WAP.getMessage().equals(aliOrderEntity.getPayType()) || PayTypeEnum.WAP.getMessage() == aliOrderEntity.getPayType()){//Wap
                //预下单成功 返回预下单信息
                resultMap.put("out_trade_no",resultJson.getString("out_trade_no"));
                resultMap.put("qr_code",resultJson.getString("qr_code"));
                return  R.ok(resultMap);
            }else {
                //二维码支付
                //返回处理过的图片base64码 前端页面直接用img标签的src接受
                String imgStr = ImageToBase64Util.createQRCode(resultJson.getString("qr_code"));
                resultMap.put("out_trade_no",resultJson.getString("out_trade_no"));
                resultMap.put("qr_code",imgStr);
                return  R.ok(resultMap);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //二维码支付

        //5.返回
        return null;
    }

    @RequestMapping(value = "/QrCodeTest" , method = RequestMethod.POST)
    public R QcCodeTestController(){
        try {
            String result = AliPayRequest.doQrCodeAliRequest(201809170952L , 1.0 , "MECHREVO");
            JSONObject resultJson = JSON.parseObject(result).getJSONObject("alipay_trade_precreate_response");
            runAsync(() -> {//异步保存
                TradePrecreateMsg tradePrecreateMsg = new TradePrecreateMsg();
                tradePrecreateMsg.setCode(resultJson.getInteger("code"));
                tradePrecreateMsg.setOrderId(201809170115L);
                tradePrecreateMsg.setMsg(resultJson.getString("msg"));
                tradePrecreateMsgService.save(tradePrecreateMsg);
            });
            String imgStr = ImageToBase64Util.createQRCode(resultJson.getString("qr_code"));
            Map resultMap = new HashMap();
            resultMap.put("out_trade_no",resultJson.getString("out_trade_no"));
            resultMap.put("qr_code",imgStr);
            return R.ok(resultMap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return R.error(90000,"未知异常，请联系管理员");
    }

    @RequestMapping(value = "AliNotify" , method = RequestMethod.POST)
    public void NotifyController(){
        //用户支付后 接受支付宝回调 处理业务逻辑 通知下游商户
    }

}
