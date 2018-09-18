package com.ntnikka.modules.orderManager.controller;

import com.ntnikka.modules.orderManager.entity.TradeOrder;
import com.ntnikka.modules.orderManager.service.TradeOrderService;
import com.ntnikka.modules.sys.controller.AbstractController;
import com.ntnikka.utils.PageUtils;
import com.ntnikka.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TradeOrderController
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/17 14:51
 **/
@RestController
@RequestMapping(value = "/tradeOrder")
public class TradeOrderController extends AbstractController {

    @Autowired
    TradeOrderService tradeOrderService;

    @RequestMapping(value = "list")
    public R queryOrderList(@RequestParam Map<String, Object> params){
        System.out.println("params = [" + params + "]");
        PageUtils page = tradeOrderService.queryPage(params);
        List orderList = page.getList().stream().collect(Collectors.toList());
        return R.ok().put("page", page);
    }

}
