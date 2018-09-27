package com.ntnikka.modules.orderManager.service;

import com.baomidou.mybatisplus.service.IService;
import com.ntnikka.modules.orderManager.entity.TradeOrder;
import com.ntnikka.utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TradeOrderService
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/17 14:55
 **/
public interface TradeOrderService extends IService<TradeOrder> {
    PageUtils queryPage(Map<String, Object> params);

    List<TradeOrder> queryList(Map<String, Object> params);
}
