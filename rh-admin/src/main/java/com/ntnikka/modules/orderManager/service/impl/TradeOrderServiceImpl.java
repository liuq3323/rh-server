package com.ntnikka.modules.orderManager.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.ntnikka.common.utils.EmptyUtil;
import com.ntnikka.common.utils.Query;
import com.ntnikka.modules.orderManager.dao.TradeOrderDao;
import com.ntnikka.modules.orderManager.entity.TradeOrder;
import com.ntnikka.modules.orderManager.service.TradeOrderService;
import com.ntnikka.utils.PageUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @ClassName TradeOrderServiceImpl
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/17 15:02
 **/
@Service("TradeOrderService")
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderDao , TradeOrder> implements TradeOrderService {

    @Autowired
    TradeOrderDao tradeOrderDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String tradeId = params.get("tradeid") == null ? "" : params.get("tradeid").toString();
        String orderId =  params.get("orderid") == null ? "" : params.get("orderid").toString();
        String merchantId =  params.get("merchantid") == null ? "" : params.get("merchantid").toString();
        String status = params.get("status") == null ? "" : params.get("status").toString();
        String star =  params.get("starttime") == null ? "" : params.get("starttime").toString();
        String end =  params.get("endtime") == null ? "" : params.get("endtime").toString();
        Page<TradeOrder> page = this.selectPage(new Query<TradeOrder>(params).getPage() ,
                                                    new EntityWrapper<TradeOrder>()
                                                    .eq(EmptyUtil.isNotEmpty(tradeId) , "id" , tradeId)
                                                    .eq(EmptyUtil.isNotEmpty(orderId) , "order_id" , orderId)
                                                    .eq(EmptyUtil.isNotEmpty(merchantId),"merchant_id", merchantId)
                                                    .eq(EmptyUtil.isNotEmpty(status), "status", status)
                                                    .ge(EmptyUtil.isNotEmpty(star), "create_time",star)
                                                    .le(EmptyUtil.isNotEmpty(end), "create_time",end));
         return new PageUtils(page);
    }
}
