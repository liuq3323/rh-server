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

    AliOrderEntity queryTradeId(Long orderId);

    void updateTradeOrder(Long orderId);

    void updateTradeStatusClosed(Long orderId);

    void updateNotifyStatus(Long orderId);

    AliOrderEntity queryById(Long id);
}
