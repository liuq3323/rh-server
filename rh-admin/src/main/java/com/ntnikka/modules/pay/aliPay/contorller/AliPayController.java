package com.ntnikka.modules.pay.aliPay.contorller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.ntnikka.common.Enum.AlipayTradeStatus;
import com.ntnikka.common.Enum.PayTypeEnum;
import com.ntnikka.common.utils.AliPayRequest;
import com.ntnikka.common.utils.HttpClientUtil;
import com.ntnikka.modules.merchantManager.entity.MerchantEntity;
import com.ntnikka.modules.merchantManager.service.MerchantService;
import com.ntnikka.modules.orderManager.entity.TradeOrder;
import com.ntnikka.modules.pay.aliPay.config.AlipayConfig;
import com.ntnikka.modules.pay.aliPay.entity.AliNotifyEntity;
import com.ntnikka.modules.pay.aliPay.entity.AliOrderEntity;
import com.ntnikka.modules.pay.aliPay.entity.TradePrecreateMsg;
import com.ntnikka.modules.pay.aliPay.entity.TradeQueryParam;
import com.ntnikka.modules.pay.aliPay.service.AliNotifyService;
import com.ntnikka.modules.pay.aliPay.service.AliOrderService;
import com.ntnikka.modules.pay.aliPay.service.TradePrecreateMsgService;
import com.ntnikka.modules.pay.aliPay.utils.*;
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
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private AliNotifyService aliNotifyService;

    @RequestMapping(value = "/create" , method = RequestMethod.POST)
    public R testController(@RequestBody AliOrderEntity aliOrderEntity){

        logger.info("进入支付宝下单-------------------------------------------");
        //1.校验必填参数
        if (!AliOrderEntity.checkParam(aliOrderEntity)){
            return R.error(403000 , "缺少参数");
        }
        MerchantEntity merchant = merchantService.queryById(aliOrderEntity.getMerchantId());
        if (merchant == null){
            return  R.error(403001,"不存在的商户");
        }
        aliOrderEntity.setPartner(merchant.getMerchantKey());
        //2.校验签名
        String sign = aliOrderEntity.getSign();
        String ParamStr = String.format("merchantId=%s&orderAmount=%s&orderId=%s&payMethod=%s&payType=%s&signType=%s&version=%s&priKey=%s",aliOrderEntity.getMerchantId(),SignUtil.doubleTrans(aliOrderEntity.getOrderAmount()),
                aliOrderEntity.getOrderId(),aliOrderEntity.getPayMethod(),aliOrderEntity.getPayType(),aliOrderEntity.getSignType(),
                aliOrderEntity.getVersion(), merchant.getMerchantKey());
        if (!SignUtil.checkSign(sign , ParamStr)){
            logger.error("验签失败, sign = {} , paramstr = {}" ,sign , ParamStr);
            return R.error(403013 , "验签失败");
        }
        //校验商户时候有权限操作
        MerchantEntity merchantEntity = merchantService.findByPriKey(aliOrderEntity.getPartner());
        if (merchantEntity == null){
            return R.error(403015, "商户不存在");
        }
        if (merchantEntity.getTradeStatus() == 1){
            return R.error(403016, "该商户已关闭交易权限，请联系客服人员");
        }
        int count = aliOrderService.checkRepeatId(aliOrderEntity.getOrderId());
        if (count > 0){
            logger.error("订单流水号重复 , orderId = {}" , aliOrderEntity.getOrderId());
            return R.error(403014, "订单流水号重复");
        }
        //3.订单信息入库
        aliOrderEntity.setMerchantId(merchantEntity.getId());
        aliOrderEntity.setCreateTime(new Date());
        aliOrderEntity.setUpdateTime(new Date());
        aliOrderService.save(aliOrderEntity);
        try {
            String result = AliPayRequest.doQrCodeAliRequest( aliOrderEntity.getOrderId() , aliOrderEntity.getOrderAmount() , aliOrderEntity.getProductName(),
                                                                merchantEntity.getAppid().toString() , merchantEntity.getMerchantPriKey() , merchantEntity.getAliPubKey() , merchantEntity.getAuthCode() , merchantEntity.getPid().toString() , merchantEntity.getStoreNumber());
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
                aliOrderService.updateTradeStatusClosed(aliOrderEntity.getOrderId());
                return R.error(403017,"下单失败").put("sub_code",resultJson.getString("sub_code")).put("sub_msg",resultJson.getString("sub_msg"));
            }
            //4.调起支付宝下单接口 根据不同的payType处理不同下单方式
            //wap支付
            //直接返回支付宝返回的二维码url
            Map resultMap = new HashMap();
            if (PayTypeEnum.WAP.getMessage().equals(aliOrderEntity.getPayType()) || PayTypeEnum.WAP.getMessage() == aliOrderEntity.getPayType()){//Wap
                //预下单成功 返回预下单信息
                resultMap.put("out_trade_no",resultJson.getString("out_trade_no"));
                resultMap.put("qr_code",resultJson.getString("qr_code"));
                return  R.ok().put("data" , resultMap);
            }else {
                //二维码支付
                //返回处理过的图片base64码 前端页面直接用img标签的src接受
                String imgStr = ImageToBase64Util.createQRCode(resultJson.getString("qr_code"));
                resultMap.put("out_trade_no",resultJson.getString("out_trade_no"));
                resultMap.put("qr_code",imgStr);
                return  R.ok().put("data" , resultMap);
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
//            String result = AliPayRequest.doQrCodeAliRequest(20180917095254L , 1.0 , "MECHREVO");
//            JSONObject resultJson = JSON.parseObject(result).getJSONObject("alipay_trade_precreate_response");
////            runAsync(() -> {//异步保存
//////                TradePrecreateMsg tradePrecreateMsg = new TradePrecreateMsg();
//////                tradePrecreateMsg.setCode(resultJson.getInteger("code"));
//////                tradePrecreateMsg.setOrderId(201809170115L);
//////                tradePrecreateMsg.setMsg(resultJson.getString("msg"));
//////                tradePrecreateMsgService.save(tradePrecreateMsg);
//////            });
//            String imgStr = ImageToBase64Util.createQRCode(resultJson.getString("qr_code"));
            Map resultMap = new HashMap();
//            resultMap.put("out_trade_no",resultJson.getString("out_trade_no"));
//            resultMap.put("qr_code",imgStr);
            MerchantEntity merchantEntity = merchantService.findByPriKey("6816CCBB9923D7B006A02C877B2D9F68");
            resultMap.put("data" , merchantEntity);
            return R.ok(resultMap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return R.error(90000,"未知异常，请联系管理员");
    }

    @RequestMapping(value = "/AliNotify" , method = RequestMethod.POST)
    public String  NotifyController(HttpServletRequest request){
        //用户支付后 接受支付宝回调 处理业务逻辑 通知下游商户
        Map<String, String> params = AliUtils.convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
        String paramsJson = JSON.toJSONString(params);
        logger.info("支付宝回调，{}", paramsJson);
        logger.info("支付宝回调参数，{}" , request.getParameterMap());
        // TODO: 2018/9/26 alipay_public_key 根据回调参数查询 非写死
        Long tradeId = Long.parseLong(params.get("out_trade_no"));
        AliOrderEntity aliOrderEntity = aliOrderService.queryTradeId(tradeId);
        if (aliOrderEntity == null){//订单不存在
            return "failure";
        }
        MerchantEntity merchantEntity = merchantService.findByPriKey(aliOrderEntity.getPartner());
        if (merchantEntity == null){//商户不存在
            return "failure";
        }
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(params, merchantEntity.getAliPubKey(),
                    AlipayConfig.input_charset_utf8 , AlipayConfig.sign_type_RSA2);
            if (signVerified){
                logger.info("支付宝回调签名认证成功");
                this.check(params);//验证业务参数
                //验证有效
                //异步处理业务数据
                runAsync(() -> {

                    String tradeStatus = params.get("trade_status");
                    if (tradeStatus.equals(AlipayTradeStatus.TRADE_SUCCESS.getStatus()) || tradeStatus.equals(AlipayTradeStatus.TRADE_FINISHED.getStatus())) {//支付完成 支付成功 处理订单状态 通知商户
                        Map<String , Object> map = new HashMap<>();
                        map.put("orderId" , tradeId);
                        map.put("tradeNo" , params.get("trade_no"));
                        aliOrderService.updateTradeOrder(map);
                        //回调入库
                        AliNotifyEntity aliNotifyEntity = new AliNotifyEntity();
                        aliNotifyEntity.setOutTradeNo(params.get("out_trade_no"));
                        aliNotifyEntity.setTradeNo(params.get("trade_no"));
                        aliNotifyEntity.setAppId(params.get("app_id"));
                        aliNotifyEntity.setBuyerId(params.get("buyer_id"));
                        aliNotifyEntity.setTradeStatus(params.get("trade_status"));
                        aliNotifyEntity.setTotalAmount(new BigDecimal(params.get("total_amount")));
                        aliNotifyEntity.setReceiptAmount(new BigDecimal(params.get("receipt_amount")));
                        aliNotifyEntity.setBuyerPayAmount(new BigDecimal(params.get("buyer_pay_amount")));
                        aliNotifyEntity.setSubject(params.get("subject"));
                        aliNotifyEntity.setGmtCreate(DateUtil.string2Date(params.get("gmt_create")));
                        aliNotifyEntity.setCreatedAt(new Date());
                        aliNotifyEntity.setUpdatedAt(new Date());
                        aliNotifyService.save(aliNotifyEntity);
                        //通知下游商户
                        // TODO: 2018/9/21
                        //测试回调暂时写死 后面修改 aliOrderEntity.getNotifyUrl()
                        String returnMsg = this.doNotify(aliOrderEntity.getNotifyUrl(),aliOrderEntity.getOrderId().toString(),AlipayTradeStatus.TRADE_SUCCESS.getStatus(),aliOrderEntity.getOrderAmount().toString(),aliOrderEntity.getPartner());
                        if (returnMsg.equals("success")){
                            logger.info("通知商户成功，修改通知状态");
                            aliOrderService.updateNotifyStatus(tradeId);
                        }else{
                            logger.error("通知商户失败");
                        }

                    }else if (tradeStatus.equals(AlipayTradeStatus.TRADE_CLOSED.getStatus())){
                            aliOrderService.updateTradeStatusClosed(tradeId);
                    }else {
                        logger.error("没有处理支付宝回调业务，支付宝交易状态: {} ", tradeStatus);
                    }
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
     * 提供商户的查询
     * @param tradeQueryParam
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping(value = "/queryOrder")
    public R queryOrderStatus(@RequestBody TradeQueryParam tradeQueryParam) throws AlipayApiException{

        logger.info("进入queryOrder ------------------------------------------------------------------");
        //1.校验签名
        MerchantEntity merchantEntity = merchantService.queryById(tradeQueryParam.getMerchantId());
        if (merchantEntity == null){
            return R.error(403015, "商户不存在");
        }
        if (merchantEntity.getTradeStatus() == 1){
            return R.error(403016, "该商户已关闭交易权限，请联系客服人员");
        }
        String sign = tradeQueryParam.getSign();
        String checkSignStr = String.format("merchantId=%s&orderId=%s&priKey=%s",tradeQueryParam.getMerchantId(),tradeQueryParam.getOrderId(),merchantEntity.getMerchantKey());
        if (!SignUtil.checkSign(sign , checkSignStr)){
            logger.error("验签失败, sign = {} , paramstr = {}" ,sign , checkSignStr);
            return R.error(403013 , "验签失败");
        }
        logger.info("验签通过--------------------------");
        //2.校验商户权限
        //校验商户时候有权限操作

        //3.调用支付宝查询接口
        String returnStr = AliPayRequest.queryOrder(merchantEntity.getAppid().toString(),merchantEntity.getMerchantPriKey(),merchantEntity.getAliPubKey(),tradeQueryParam.getOrderId() , merchantEntity.getAuthCode());
        logger.info("支付宝返回msg : {}" , returnStr);
        JSONObject resultJson = JSON.parseObject(returnStr).getJSONObject("alipay_trade_query_response");
        Integer code = resultJson.getInteger("code");
        if(code != 10000){
            if (code == 40004){
                return R.error(40004 , "用户支付中..");
            }
            return R.error(40000 , resultJson.getString("msg"));
        }
        //交易状态成功 修改
        String trade_status = resultJson.getString("trade_status");
        String out_trade_no = resultJson.getString("out_trade_no");
        if (trade_status.equals(AlipayTradeStatus.TRADE_SUCCESS.getStatus()) || trade_status.equals(AlipayTradeStatus.TRADE_FINISHED.getStatus())){
            Map<String , Object> map = new HashMap<>();
            map.put("orderId" , tradeQueryParam.getOrderId());
            map.put("tradeNo" , resultJson.get("trade_no"));
            aliOrderService.updateTradeOrder(map);
//            aliOrderService.updateTradeOrder(tradeQueryParam.getOrderId());
        }
        Map<String , Object> map = new HashMap<>();
        map.put("trade_status" , trade_status);
        map.put("orderId" , out_trade_no);
        return R.ok().put("data" , map);
    }

    @RequestMapping(value = "/queryOrderByAdmin")
    public R queryOrderStatus(@RequestBody Map map) throws AlipayApiException{

        logger.info("进入queryOrder By Admin ------------------------------------------------------------------");
        Long id = Long.parseLong(map.get("id").toString());
        AliOrderEntity aliOrderEntity = aliOrderService.queryById(id);
        if (aliOrderEntity == null){
            return R.error(50000,"订单不存在");
        }
        MerchantEntity merchantEntity = merchantService.findByPriKey(aliOrderEntity.getPartner());
        if (merchantEntity == null){
            return R.error(50001,"商户不存在");
        }
        String returnStr = AliPayRequest.queryOrder(merchantEntity.getAppid().toString(),merchantEntity.getMerchantPriKey(),merchantEntity.getAliPubKey() , aliOrderEntity.getOrderId() , merchantEntity.getAuthCode());
        logger.info("支付宝返回msg : {}" , returnStr);
        JSONObject resultJson = JSON.parseObject(returnStr).getJSONObject("alipay_trade_query_response");
        Integer code = resultJson.getInteger("code");
        if(code != 10000){
            if (code == 40004){
                return R.error(40004 , "用户支付中..");
            }
            return R.error(40000 , resultJson.getString("msg"));
        }
        String trade_status = resultJson.getString("trade_status");
        if (trade_status.equals(AlipayTradeStatus.TRADE_SUCCESS.getStatus()) || trade_status.equals(AlipayTradeStatus.TRADE_FINISHED.getStatus())){
            Map<String , Object> paramMap = new HashMap<>();
            paramMap.put("orderId" , aliOrderEntity.getOrderId());
            paramMap.put("tradeNo" , resultJson.get("trade_no"));
            aliOrderService.updateTradeOrder(paramMap);
//          aliOrderService.updateTradeOrder(aliOrderEntity.getOrderId());
            //客服人员主动查询 若已支付成功则主动发送通知
            String returnMsg = this.doNotify(aliOrderEntity.getNotifyUrl(),aliOrderEntity.getOrderId().toString(),AlipayTradeStatus.TRADE_SUCCESS.getStatus(),aliOrderEntity.getOrderAmount().toString(),aliOrderEntity.getPartner());
            if (returnMsg.equals("success")){
                logger.info("通知商户成功，修改通知状态");
                aliOrderService.updateNotifyStatus(aliOrderEntity.getOrderId());
            }else{
                logger.error("通知商户失败");
            }
            return R.ok("已支付");
        }else if (trade_status.equals(AlipayTradeStatus.TRADE_CLOSED.getStatus())){
            aliOrderService.updateTradeStatusClosed(aliOrderEntity.getOrderId());
            return R.error(60000,"订单失败");
        }else {
            return R.ok("用户支付中..");
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
        logger.info("支付宝返回total_amount : {}" , params.get("total_amount"));
        logger.info("订单total_amount : {}" , order.getOrderAmount().toString());
        Double total_amount = Double.parseDouble(params.get("total_amount"));
//        if (total_amount != order.getOrderAmount()) {
//            throw new AlipayApiException("error total_amount");
//        }

        // 3、校验通知中的seller_id（或者seller_email)是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
        // 第三步可根据实际情况省略

        // 4、验证app_id是否为该商户本身。
        MerchantEntity merchantEntity = merchantService.findByPriKey(order.getPartner());
        if (merchantEntity == null) {
            throw new AlipayApiException("app_id不存在");
        }
        if (!params.get("app_id").equals(merchantEntity.getAppid().toString())) {
            throw new AlipayApiException("app_id不一致");
        }
    }

    @RequestMapping(value = "pageTest" , method = RequestMethod.POST)
    public R payTest(){
        runAsync(() -> {
            String url = "http://localhost/api/v1/notify";
            Map<String , String> params = new HashMap<>();
            params.put("out_trade_no" , "12115448574");
            params.put("trade_status" , AlipayTradeStatus.TRADE_SUCCESS.getStatus());
            params.put("totalAmount" , "1");
            String rreturnStr = HttpClientUtil.doPost(url, params);
            if (rreturnStr.equals("success")){
                logger.info("通知商户成功，修改通知状态");
            }else{
                logger.error("通知商户失败");
            }
        });
        return R.ok().put("data" , "test Ok !!!!!!!!!");
    }

    @RequestMapping(value = "notify" , method = RequestMethod.POST)
    public String testNotify(HttpServletRequest request){
        logger.info("进入模拟商户回调:{}", request);
        return "success";
    }

    @RequestMapping(value = "testAuth" , method = RequestMethod.POST)
    public R testAuthToken() throws AlipayApiException{
        String appId = "2018091361348399";
        String priKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDEEDJ/VWM5YjjX Su0VPB3hSydKF8/jD8kplSXZPCnkBZJplH01ZY0SM6uzJ9f/LelVvgYBSusnw9NC erPM4W2/rjnrRnw4ZrLKJw29m5ueK2bZMTrufaQhBZHcFqNbtbBLfaoTd0gTDBgw klup88t7M8nvJVj1ZF+tVawVV3U2kT+TNW2A422wld4pTWNNDNofojdLT49yDvJU mYw9cCm/U9oWt5STfyYqQnn3LFJdtZboDvCppSzovhrCUxrgAdSKE+PjvirCePim xi5yr/u/9YHcl5+zl/YvEgh5Ab0QWWDGgi3BdjYdQWy/gk6mBYKrp4KbPOt4qZ9R zIKJfpn3AgMBAAECggEBALntdkXEbr0rRSX9WskpYliVEWQ1IqJ8BNMXKnZQlJU1 J3xSIU6yx838DBZwcWf/XOg/tKgjKM9j6AKCI+Hl4VLF4Q0ZoZFG6sPDt0cYusGK /RR4mB80LKJYCtNA8Jd2vAFK4S9mjYXqkUH2eVC47j0ehp/vteW30veoZ/ExJ+me tlw70cxONEE134MzelFpqSPKR+SAGajLTQmfJleK2VP0zXnXol03+dknOGMmehkG +24bTqoLkLgq53eS45aRr+KAhgvw8YzdzJR09MvSazk3EVRJNVflfsVPr8l9SeI0 ua63n+Y+5ivL5RRWG+FwUxIMuGyDXj71EaEhqEgIN5kCgYEA/Cqk3uFFF4di8Qfa RIO4kw0uEEibCAIQY3Dlm3++/8Q/qyVXM3bXZNXoMbzkjnWy0h3ObyU4deJiJWvt yJhYueRRozPkvnPIRxoZmW5UmUMMpKygp/95qwluLrygOd8qXGxj7HTX/NsekbFp Mg6X3mVSr1OJyzVLRhEkwTX7/J0CgYEAxws3VRuHvrPsLV0218TX86EC1QyIkaps p7Dq1hMyO8J/ddWpmabZzncCwa7RxH5FuB6FtEWT/vdzEoLmzCWOn9r44eqFtCup 7ZfSiwEbKLTL/JT520wwbFgLQs5U72qLUJuS9b4YzaBsPy2J5ZL5g1QPsBXSbaXh ZG9oAXqXKqMCgYBHLLowdqELzRjuM2s2H3+/cd7olbW8guihSMJmK557jqbx5a5B Rm/xdDb/ovNYCnyYtfUIhhbznxxYt6f8EJQqk/k++Wy9HO0QtJs5JV0XjUdKS7dk 88Uhs52372HCM+0+/REPMsOsCSUNtmecy2WuTicSeZ/RDY/bjQc0ycoAOQKBgAv8 zfFPPiarGV9GDOaD0Wm32b7RCWyUcgTLb+lCsLTOqvSC0LsTRGzd2AmoKLQH0tUa 6XyI8Dfb2U9VGOdUn7la68BXaoQGWS6ZBTIf9+3ErcBhjIa7mPL9fnggixb8OLW7 GOe58i54KezIJlTwPBy/tE4rkkfORLvSxdXI90+3AoGAMl8A+arhdkLfpf+vW7st ARL2oKd6JDhXm5SK8pcdzGSs+EheTXicdGq8y1ROCp3D7D4bpCKsHgpuYhP1GvOk xLXlJ2PMXI9QhCeeGtDehbJAD47+2NBg0KjXaaW+AJQFSYfIZXTb8FMVDOHg7blk KM9+c6kiVG1YSssE8Rcfum0=";
        String aliPubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAounwJ0cIGZLbI5m8CkqJydwl8sFlM1L1gTu10Cv5GZ76kxrRmi3KmSqNbkmY/lsQHhtmVXmSVPH/4ona0p8sNvpnnxxVLC+a2nJHgmDR59/7H+B6D4qMXW3CCHO1DCrtRvokwWb7+OLSIrrteNOGX2lg3hiRRD90UE1JBNOqX4601SYoUO7c4BPKCqQ371w2BfsTAv5jVn68soMMX9MLxICfOe58AvlpVBbe8JwBIVnW82gxyfYTBSbLQOPPSCreBlZ5PiBNvRZwEehd5RQLhCX2YB0ieq/vDLvi5Okzmjjqx8h9CR687Q0WQZ4Y+7tc68XBVd5xpv4TSz6SbwImmQIDAQAB";
        String authCode = "201809BB5d3abaf5424b441cbb09f4c15cb0aX07";
        String authTokenStr = AliPayRequest.getAuthToken(appId , priKey , aliPubKey , authCode);
        logger.info("authToken String : {}" , authTokenStr);
        return R.ok();
    }

    private String doNotify(String url , String out_trade_no , String trade_status , String total_amount , String partner){
        Map<String , String> params = new HashMap<>();
        params.put("out_trade_no" , out_trade_no);
        params.put("trade_status" , trade_status);
        params.put("totalAmount" , total_amount);
        String signStr = "orderAmount="+total_amount+"&orderId="+out_trade_no+"&priKey="+partner;
        String sign = MD5Utils.encode(signStr).toUpperCase();
        params.put("sign",sign);
        return HttpClientUtil.doPost(url, params);
    }

}
