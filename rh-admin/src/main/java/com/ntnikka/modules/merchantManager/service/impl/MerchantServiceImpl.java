package com.ntnikka.modules.merchantManager.service.impl;

import com.ntnikka.common.utils.Query;
import com.ntnikka.modules.merchantManager.dao.MerchantDao;
import com.ntnikka.modules.merchantManager.entity.MerchantEntity;
import com.ntnikka.modules.merchantManager.service.MerchantService;
import com.ntnikka.utils.PageUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;



@Service("merchantService")
public class MerchantServiceImpl extends ServiceImpl<MerchantDao, MerchantEntity> implements MerchantService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<MerchantEntity> page = this.selectPage(
                new Query<MerchantEntity>(params).getPage(),
                new EntityWrapper<MerchantEntity>()
        );

        return new PageUtils(page);
    }

}
