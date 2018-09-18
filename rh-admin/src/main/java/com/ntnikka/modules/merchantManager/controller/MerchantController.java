package com.ntnikka.modules.merchantManager.controller;

import java.util.Arrays;
import java.util.Map;

import com.ntnikka.modules.merchantManager.entity.MerchantEntity;
import com.ntnikka.modules.merchantManager.service.MerchantService;
import com.ntnikka.modules.pay.aliPay.utils.MD5Utils;
import com.ntnikka.utils.PageUtils;
import com.ntnikka.utils.R;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;




/**
 *
 * @author Liuq
 * @email
 * @date 2018-09-18 16:41:11
 *
 */
@RestController
@RequestMapping("/merchant/mgr")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = merchantService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
			MerchantEntity merchant = merchantService.selectById(id);

        return R.ok().put("merchant", merchant);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MerchantEntity merchant){
        merchant.setMerchantKey(MD5Utils.creatMerchantKey(merchant));
        merchantService.insert(merchant);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MerchantEntity merchant){
			merchantService.updateById(merchant);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
			merchantService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }

}
