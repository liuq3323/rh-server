package com.ntnikka.modules.merchantManager.service.impl;

import com.ntnikka.common.utils.PageUtils;
import com.ntnikka.common.utils.Query;
import com.ntnikka.modules.merchantManager.dao.AppIdDao;
import com.ntnikka.modules.merchantManager.entity.AppIdEntity;
import com.ntnikka.modules.merchantManager.service.AppIdService;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;



@Service("appIdService")
public class AppIdServiceImpl extends ServiceImpl<AppIdDao, AppIdEntity> implements AppIdService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<AppIdEntity> page = this.selectPage(
                new Query<AppIdEntity>(params).getPage(),
                new EntityWrapper<AppIdEntity>()
        );

        return new PageUtils(page);
    }

}
