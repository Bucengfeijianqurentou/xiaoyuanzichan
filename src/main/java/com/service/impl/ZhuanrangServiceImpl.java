package com.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.utils.PageUtils;
import com.utils.Query;

import com.dao.ZhuanrangDao;
import com.entity.ZhuanrangEntity;
import com.service.ZhuanrangService;
import com.entity.view.ZhuanrangView;

/**
 * 资产转让 服务实现类
 */
@Service("zhuanrangService")
public class ZhuanrangServiceImpl extends ServiceImpl<ZhuanrangDao, ZhuanrangEntity> implements ZhuanrangService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<ZhuanrangEntity> page = this.selectPage(
                new Query<ZhuanrangEntity>(params).getPage(),
                new EntityWrapper<ZhuanrangEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public List<ZhuanrangView> selectListView(Wrapper<ZhuanrangEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public ZhuanrangView selectView(Wrapper<ZhuanrangEntity> wrapper) {
		return baseMapper.selectListView(wrapper).get(0);
	}
} 