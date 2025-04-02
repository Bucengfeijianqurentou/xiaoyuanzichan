package com.dao;

import com.entity.ZhuanrangEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.view.ZhuanrangView;

/**
 * 资产转让 Dao 接口
 */
public interface ZhuanrangDao extends BaseMapper<ZhuanrangEntity> {
	
	List<ZhuanrangView> selectListView(@Param("ew") Wrapper<ZhuanrangEntity> wrapper);
	
	List<ZhuanrangView> selectListView(Pagination page,@Param("ew") Wrapper<ZhuanrangEntity> wrapper);
	
} 