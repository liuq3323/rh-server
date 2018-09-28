package com.ntnikka.modules.orderManager.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ntnikka.common.utils.ExcelUtil;
import com.ntnikka.modules.orderManager.entity.TradeBarChartEntity;
import com.ntnikka.modules.orderManager.entity.TradeOrder;
import com.ntnikka.modules.orderManager.service.TradeOrderService;
import com.ntnikka.modules.pay.aliPay.utils.DateUtil;
import com.ntnikka.modules.sys.controller.AbstractController;
import com.ntnikka.utils.DateUtils;
import com.ntnikka.utils.PageUtils;
import com.ntnikka.utils.R;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
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

    private static final String[] title = {"序号","商户ID","商户名称","支付方式","商户订单号","系统订单号","银行订单号","订单金额","支付时间","通知状态"};

    @Autowired
    TradeOrderService tradeOrderService;

    @RequestMapping(value = "list")
    public R queryOrderList(@RequestParam Map<String, Object> params){
        System.out.println("params = [" + params + "]");
        PageUtils page = tradeOrderService.queryPage(params);
        Map<String , Object> map = new HashMap<>();
        map.put("totalCount" , 100);
        map.put("totalSuccessCount" , 65);
        map.put("totalAmount" , 500000);
        map.put("totalSuccessAmount" , 320000);
        return R.ok().put("page", page).put("countDate",map);
    }

    /**
     * 导出列表
     * @return
     */
    @RequestMapping(value = "/export")
    public void export(HttpServletRequest request , HttpServletResponse response) throws Exception {

        //excel文件名
        String fileName = "订单信息"+System.currentTimeMillis()+".xls";
        //sheet名
        String sheetName = "订单信息";

        Map<String , Object> params = new HashMap();

        params.put("tradeid" , request.getParameter("tradeid"));
        params.put("orderid" , request.getParameter("orderid"));
        params.put("starttime" , request.getParameter("starttime"));
        params.put("endtime" , request.getParameter("endtime"));
        params.put("merchantid" , request.getParameter("merchantid"));
        params.put("status" , request.getParameter("status"));
        params.put("merchantdept" , request.getParameter("merchantdept"));
        //orderList
        List<TradeOrder> orderList = tradeOrderService.queryList(params);

        String[][] content = new String[orderList.size()][];

        for (int i = 0; i < orderList.size(); i++) {
            TradeOrder order = orderList.get(i);
            content[i] = new String[title.length];
            content[i][0] = order.getId().toString();
            content[i][1] = order.getMerchantId().toString();
            content[i][2] = order.getMerchantName();
            content[i][3] = order.getPayType().equals("Wap")? "Wap支付" : "二维码支付";
            content[i][4] = order.getOrderId();
            content[i][5] = order.getSysTradeNo() == null ? "" : order.getSysTradeNo();
            content[i][6] = order.getTradeNo() == null ? "" : order.getTradeNo();
            content[i][7] = order.getOrderAmount().toString();
            content[i][8] = order.getPayTime() == null ? "" : DateUtil.Date2Str(order.getPayTime());
            content[i][9] = order.getNotifyStatus() == 0 ? "未通知" : "已通知";
        }

        //创建HSSFWorkbook
        HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook(sheetName, title, content, null);

        //响应到客户端
        try {
            this.setResponseHeader(response, fileName);
            OutputStream os = response.getOutputStream();
            wb.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //发送响应流方法
    public void setResponseHeader(HttpServletResponse response, String fileName) {
        try {
            try {
                fileName = new String(fileName.getBytes(),"ISO8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            response.setContentType("application/vnd.ms-excel;charset=ISO8859-1");
            response.setHeader("Content-Disposition", "attachment;filename="+ fileName);
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @RequestMapping(value = "testCount")
    public R testCount(){
        List<String> nameList = new ArrayList<>();
        List<Double> amountList = new ArrayList<>();
        nameList.add("test1");
        nameList.add("test2");
        nameList.add("test3");
        nameList.add("test4");
        amountList.add(100D);
        amountList.add(1000.5D);
        amountList.add(256.7D);
        amountList.add(695.4D);
        Map map = new HashMap();
        Date da = new Date();
        Calendar calendar = Calendar.getInstance();
        //得到日历
        calendar.setTime(da);
        // 把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH,-3);
        // 设置为前一天
        Date end = calendar.getTime();//获取2个月前的时间
        map.put("start" , da);
        map.put("end" , end);
        List<TradeOrder> tradeBarChart = tradeOrderService.queryOrderDataForBarChart(map);
        return R.ok().put("nameList", nameList).put("amountList",amountList);
    }

}
