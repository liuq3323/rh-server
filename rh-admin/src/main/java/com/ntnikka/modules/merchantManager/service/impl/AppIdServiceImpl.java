package com.ntnikka.modules.merchantManager.service.impl;

import com.ntnikka.common.utils.EmptyUtil;
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
        String appid = params.get("appid") == null ? "" : params.get("appid").toString();
        Page<AppIdEntity> page = this.selectPage(
                new Query<AppIdEntity>(params).getPage(),
                new EntityWrapper<AppIdEntity>().eq(EmptyUtil.isNotEmpty(appid) , "appid" , appid)
        );

        return new PageUtils(page);
    }

}
