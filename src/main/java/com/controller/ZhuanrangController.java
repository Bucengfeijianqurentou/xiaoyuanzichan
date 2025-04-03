package com.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.annotation.IgnoreAuth;

import com.entity.ZhuanrangEntity;
import com.entity.view.ZhuanrangView;
import com.service.ZhuanrangService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.MPUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * 资产转让 后端接口
 */
@RestController
@RequestMapping("/zhuanrang")
public class ZhuanrangController {
    @Autowired
    private ZhuanrangService zhuanrangService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,ZhuanrangEntity zhuanrang){
        EntityWrapper<ZhuanrangEntity> ew = new EntityWrapper<ZhuanrangEntity>();
        PageUtils page = zhuanrangService.queryPage(params);
        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,ZhuanrangEntity zhuanrang){
        EntityWrapper<ZhuanrangEntity> ew = new EntityWrapper<ZhuanrangEntity>();
        PageUtils page = zhuanrangService.queryPage(params);
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list(ZhuanrangEntity zhuanrang){
        EntityWrapper<ZhuanrangEntity> ew = new EntityWrapper<ZhuanrangEntity>();
        ew.allEq(MPUtil.allEQMapPre( zhuanrang, "zhuanrang")); 
        return R.ok().put("data", zhuanrangService.selectListView(ew));
    }

    /**
     * 查看详情
     */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        ZhuanrangEntity zhuanrang = zhuanrangService.selectById(id);
        return R.ok().put("data", zhuanrang);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        ZhuanrangEntity zhuanrang = zhuanrangService.selectById(id);
        return R.ok().put("data", zhuanrang);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody ZhuanrangEntity zhuanrang, HttpServletRequest request){
        String userId = String.valueOf(request.getSession().getAttribute("userId"));
        if(userId == null || "".equals(userId)){
            return R.error(511,"请先登录");
        }
        zhuanrang.setFromId(Integer.valueOf(userId));
        zhuanrang.setCreateTime(new Date());
        zhuanrangService.insert(zhuanrang);
        return R.ok();
    }
    
    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody ZhuanrangEntity zhuanrang){
        zhuanrangService.updateById(zhuanrang);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        zhuanrangService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 批量查询资产的转让状态
     */
    @RequestMapping("/status")
    public R getTransferStatus(@RequestBody Map<String, List<Integer>> params) {
        List<Integer> assetIds = params.get("assetIds");
        if (assetIds == null || assetIds.isEmpty()) {
            return R.error("资产ID列表不能为空");
        }

        EntityWrapper<ZhuanrangEntity> ew = new EntityWrapper<>();
        ew.in("shangpin_id", assetIds);
        // 获取最新的转让记录，按创建时间倒序
        ew.orderBy("create_time", false);

        List<ZhuanrangEntity> transfers = zhuanrangService.selectList(ew);
        
        // 构建资产ID到转让状态的映射
        Map<Integer, Integer> statusMap = new HashMap<>();
        for (ZhuanrangEntity transfer : transfers) {
            // 只记录每个资产的最新状态（因为按创建时间倒序，第一次遇到的就是最新的）
            if (!statusMap.containsKey(transfer.getShangpinId())) {
                statusMap.put(transfer.getShangpinId(), transfer.getStatus());
            }
        }

        return R.ok().put("data", statusMap);
    }
} 