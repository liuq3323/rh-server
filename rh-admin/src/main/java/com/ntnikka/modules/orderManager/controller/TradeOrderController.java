package com.ntnikka.modules.orderManager.controller;

import com.ntnikka.modules.sys.controller.AbstractController;
import com.ntnikka.utils.PageUtils;
import com.ntnikka.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName TradeOrderController
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/17 14:51
 **/
@RestController
@RequestMapping(value = "/tradeOrder")
public class TradeOrderController extends AbstractController {

    @RequestMapping(value = "list" , method = RequestMethod.POST)
    public R queryOrderList(@RequestParam Map<String, Object> params){

        return null;
    }

}
