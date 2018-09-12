package com.ntnikka.modules.pay.aliPay.contorller;


import com.ntnikka.modules.pay.aliPay.entity.AliOrderEntity;
import com.ntnikka.modules.pay.aliPay.service.AliOrderService;
import com.ntnikka.modules.pay.aliPay.utils.SignUtil;
import com.ntnikka.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Created by liuq on 2018/9/11.
 */

@RestController
@RequestMapping("/api/v1")
public class AliPayController extends BaseController{

    @Autowired
    private AliOrderService aliOrderService;

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
        // TODO: 2018/9/11
        //3.订单信息入库
        aliOrderEntity.setCreateTime(new Date());
        aliOrderEntity.setUpdateTime(new Date());
        aliOrderService.save(aliOrderEntity);
        //4.调起支付宝下单接口
        //5.返回
        return R.ok().put("data","test OK!");
    }
}
