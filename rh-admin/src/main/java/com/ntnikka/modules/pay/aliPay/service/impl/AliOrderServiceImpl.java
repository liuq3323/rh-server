package com.ntnikka.modules.pay.aliPay.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ntnikka.modules.pay.aliPay.dao.AliOrderDao;
import com.ntnikka.modules.pay.aliPay.entity.AliOrderEntity;
import com.ntnikka.modules.pay.aliPay.entity.TradePrecreateMsg;
import com.ntnikka.modules.pay.aliPay.service.AliOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by liuq on 2018/9/11.
 */
@Service("AliOrderService")
public class AliOrderServiceImpl extends ServiceImpl<AliOrderDao , AliOrderEntity> implements AliOrderService{

    @Autowired
    private AliOrderDao aliOrderDao;

    @Override
    public void save(AliOrderEntity aliOrderEntity) {
        this.insert(aliOrderEntity);
    }

    @Override
    public int checkRepeatId(Long orderId) {
        return aliOrderDao.checkRepeatId(orderId);
    }

    @Override
    public AliOrderEntity queryTradeId(Long orderId) {
        return aliOrderDao.queryByTradeId(orderId);
    }

    @Override
    public void updateTradeOrder(Long orderId) {
        aliOrderDao.updateTradeStatus(orderId);
    }

    @Override
    public void updateTradeStatusClosed(Long orderId) {
        aliOrderDao.updateTradeStatusClosed(orderId);
    }

    public void updateNotifyStatus(Long orderId){
        aliOrderDao.updateNotifyStatus(orderId);
    }
}
