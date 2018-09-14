package com.ntnikka.modules.pay.aliPay.service;

import com.baomidou.mybatisplus.service.IService;
import com.ntnikka.modules.pay.aliPay.entity.AliOrderEntity;
import com.ntnikka.modules.pay.aliPay.entity.TradePrecreateMsg;

/**
 * Created by liuq on 2018/9/11.
 */
public interface AliOrderService extends IService<AliOrderEntity> {
    void save(AliOrderEntity aliOrderEntity);

    int checkRepeatId(Long orderId);

    void save(TradePrecreateMsg tradePrecreateMsg);
}
