package com.ntnikka.modules.orderManager.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ntnikka.modules.orderManager.entity.TradeBarChartEntity;
import com.ntnikka.modules.orderManager.entity.TradeOrder;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName TradeOrderDao
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/17 14:50
 **/
@Repository
public interface TradeOrderDao extends BaseMapper<TradeOrder> {
    List<TradeOrder> queryOrderDataForBarChart(Map map);
}
