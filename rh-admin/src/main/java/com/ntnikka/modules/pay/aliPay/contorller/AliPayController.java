package com.ntnikka.modules.pay.aliPay.contorller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.ntnikka.common.Enum.AlipayTradeStatus;
import com.ntnikka.common.Enum.PayTypeEnum;
import com.ntnikka.common.utils.AliPayRequest;
import com.ntnikka.modules.orderManager.entity.TradeOrder;
import com.ntnikka.modules.pay.aliPay.config.AlipayConfig;
import com.ntnikka.modules.pay.aliPay.entity.AliOrderEntity;
import com.ntnikka.modules.pay.aliPay.entity.TradePrecreateMsg;
import com.ntnikka.modules.pay.aliPay.service.AliOrderService;
import com.ntnikka.modules.pay.aliPay.service.TradePrecreateMsgService;
import com.ntnikka.modules.pay.aliPay.utils.AliUtils;
import com.ntnikka.modules.pay.aliPay.utils.ImageToBase64Util;
import com.ntnikka.modules.pay.aliPay.utils.SignUtil;
import com.ntnikka.modules.sys.controller.AbstractController;
import com.ntnikka.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
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

    private static Logger logger = LoggerFactory.getLogger(AliPayController.class);

    @Autowired
    private AliOrderService aliOrderService;

    @Autowired
    private TradePrecreateMsgService tradePrecreateMsgService;

    @RequestMapping(value = "/test" , method = RequestMethod.POST)
    public R testController(@RequestBody AliOrderEntity aliOrderEntity){
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
                tradePrecreateMsgService.save(tradePrecreateMsg);
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
    public String  NotifyController(HttpServletRequest request){
        //用户支付后 接受支付宝回调 处理业务逻辑 通知下游商户
        Map<String, String> params = AliUtils.convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
        String paramsJson = JSON.toJSONString(params);
        logger.info("支付宝回调，{}", paramsJson);
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key,
                    AlipayConfig.input_charset_utf8 , AlipayConfig.sign_type_RSA2);
            if (signVerified){
                logger.info("支付宝回调签名认证成功");
                this.check(params);//验证业务参数
                //异步处理业务数据
                runAsync(() -> {
                    Long tradeId = Long.parseLong(params.get("out_trade_no"));
                    String tradeStatus = params.get("trade_status");
                    if (tradeStatus.equals(AlipayTradeStatus.TRADE_SUCCESS.getStatus())
                            || tradeStatus.equals(AlipayTradeStatus.TRADE_FINISHED.getStatus())) {//支付完成 支付成功 处理订单状态 通知商户
                            aliOrderService.updateTradeOrder(tradeId);
                    }else if (tradeStatus.equals(AlipayTradeStatus.TRADE_CLOSED.getStatus())){
                            aliOrderService.updateTradeStatusClosed(tradeId);
                    }else {
                        logger.error("没有处理支付宝回调业务，支付宝交易状态:", tradeStatus);
                    }
                    //通知下游商户
                    // TODO: 2018/9/20  
                });
                return "success";
            }else {
                logger.error("支付宝回调签名认证失败，signVerified=false, paramsJson:{}", paramsJson);
                return "failure";
            }
        }catch (AlipayApiException e){
            logger.error("支付宝回调签名认证失败,paramsJson:{},errorMsg:{}", paramsJson, e.getMessage());
            return "failure";
        }
    }


    /**
     * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
     * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
     * 3、校验通知中的seller_id（或者seller_email)是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
     * 4、验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
     * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
     * 在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
     *
     * @param params
     * @throws AlipayApiException
     */
    private void check(Map<String, String> params) throws AlipayApiException {
        String outTradeNo = params.get("out_trade_no");

        // 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        // TODO: 2018/9/19 根据out_trade_no查询订单是否存在
        AliOrderEntity order = aliOrderService.queryTradeId(Long.parseLong(outTradeNo));
        if (order == null) {
            throw new AlipayApiException("out_trade_no错误");
        }

        // 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        long total_amount = new BigDecimal(params.get("total_amount")).multiply(new BigDecimal(100)).longValue();
        if (total_amount != order.getOrderAmount().longValue()) {
            throw new AlipayApiException("error total_amount");
        }

        // 3、校验通知中的seller_id（或者seller_email)是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
        // 第三步可根据实际情况省略

        // 4、验证app_id是否为该商户本身。
        if (!params.get("app_id").equals(AlipayConfig.app_id)) {
            throw new AlipayApiException("app_id不一致");
        }
    }



}
