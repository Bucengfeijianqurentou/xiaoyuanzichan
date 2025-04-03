package com.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.BeanUtils;

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
import com.entity.YonghuEntity;
import com.service.YonghuService;
import com.entity.vo.ZhuanrangVO;

/**
 * 资产转让 服务实现类
 */
@Service("zhuanrangService")
public class ZhuanrangServiceImpl extends ServiceImpl<ZhuanrangDao, ZhuanrangEntity> implements ZhuanrangService {

    private final YonghuService yonghuService;

    public ZhuanrangServiceImpl(YonghuService yonghuService) {
        this.yonghuService = yonghuService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<ZhuanrangEntity> page = this.selectPage(
                new Query<ZhuanrangEntity>(params).getPage(),
                new EntityWrapper<ZhuanrangEntity>()
        );
        
        Page<ZhuanrangVO> voPage = new Page<>();
        List<ZhuanrangVO> voList = new ArrayList<>();
        
        // 转换为VO对象并添加用户信息
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            for (ZhuanrangEntity record : page.getRecords()) {
                ZhuanrangVO vo = new ZhuanrangVO();
                BeanUtils.copyProperties(record, vo);
                
                // 获取转让方用户信息
                YonghuEntity fromUser = yonghuService.selectById(record.getFromId());
                if (fromUser != null) {
                    vo.setFromUserName(fromUser.getYonghuName());
                }
                
                // 获取接收方用户信息
                YonghuEntity toUser = yonghuService.selectById(record.getToId());
                if (toUser != null) {
                    vo.setToUserName(toUser.getYonghuName());
                }
                
                voList.add(vo);
            }
        }
        
        voPage.setRecords(voList);
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());
        
        return new PageUtils(voPage);
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