package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.ZhuanrangEntity;
import java.util.List;
import java.util.Map;
import com.entity.view.ZhuanrangView;

/**
 * 资产转让 服务类接口
 */
public interface ZhuanrangService extends IService<ZhuanrangEntity> {

    /**
     * 分页查询
     */
    PageUtils queryPage(Map<String, Object> params);
    
    List<ZhuanrangView> selectListView(Wrapper<ZhuanrangEntity> wrapper);
    
    ZhuanrangView selectView(Wrapper<ZhuanrangEntity> wrapper);
} 