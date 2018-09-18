package com.ntnikka.modules.merchantManager.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ntnikka.modules.merchantManager.entity.MerchantDept;

import java.util.List;

/**
 * @ClassName MerchantDeptDao
 * @Author Liuq
 * @Description todo
 * @Date 2018/9/18 14:15
 **/
public interface MerchantDeptDao extends BaseMapper<MerchantDept> {

    /**
     * 查询子部门ID列表
     * @param parentId  上级部门ID
     */
    List<Long> queryDetpIdList(Long parentId);
}
