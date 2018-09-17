package com.ntnikka.modules.orderManager.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ntnikka.modules.orderManager.dao.TradeOrderDao;
import com.ntnikka.modules.orderManager.entity.TradeOrder;
import com.ntnikka.modules.orderManager.service.TradeOrderService;
import org.springframework.stereotype.Service;

/**
 * @ClassName TradeOrderServiceImpl
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/17 15:02
 **/
@Service("TradeOrderService")
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderDao , TradeOrder> implements TradeOrderService {
}
