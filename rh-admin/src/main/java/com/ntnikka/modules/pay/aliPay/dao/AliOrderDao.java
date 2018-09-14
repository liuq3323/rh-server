package com.ntnikka.modules.pay.aliPay.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ntnikka.modules.pay.aliPay.entity.AliOrderEntity;
import com.ntnikka.modules.pay.aliPay.entity.TradePrecreateMsg;
import org.springframework.stereotype.Repository;

/**
 * Created by liuq on 2018/9/11.
 */
@Repository
public interface AliOrderDao extends BaseMapper<AliOrderEntity>{
    int checkRepeatId(Long orderId);

    void savetradePrecreateMsg(TradePrecreateMsg tradePrecreateMsg);
}
