package com.controller;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.annotation.IgnoreAuth;
import com.entity.TransactionEntity;
import com.entity.view.TransactionView;
import com.service.DictionaryService;
import com.service.TokenService;
import com.service.TransactionService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 交易记录
 * 后端接口
 */
@RestController
@RequestMapping("/transaction")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;

    /**
     * 后端列表
     */
    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        logger.debug("page方法:,,Controller:{},,params:{}", this.getClass().getName(), JSONObject.toJSONString(params));
        
        // 暂时移除权限检查，便于测试
        // String role = String.valueOf(request.getSession().getAttribute("role"));
        // if (StringUtil.isEmpty(role))
        //    return R.error(511, "权限为空");
        
        // 默认按ID降序排序
        if (params.get("orderBy") == null || params.get("orderBy").equals("")) {
            params.put("orderBy", "id");
        }
        
        PageUtils page = transactionService.queryPage(params);

        // 字典表数据转换
        List<TransactionView> list = (List<TransactionView>) page.getList();
        for (TransactionView c : list) {
            // 修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
     * 根据ID查询
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request) {
        logger.debug("info方法:,,Controller:{},,id:{}", this.getClass().getName(), id);
        TransactionEntity transaction = transactionService.selectById(id);
        
        if (transaction == null) {
            return R.error("交易记录不存在");
        }
        
        return R.ok().put("data", transaction);
    }
    
    /**
     * 根据交易哈希查询
     */
    @RequestMapping("/hashInfo")
    public R hashInfo(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        logger.debug("hashInfo方法:,,Controller:{},,params:{}", this.getClass().getName(), JSONObject.toJSONString(params));
        
        String transactionHash = (String) params.get("transactionHash");
        if (StringUtil.isEmpty(transactionHash)) {
            return R.error("交易哈希不能为空");
        }
        
        // 创建查询条件
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("transactionHash", transactionHash);
        
        PageUtils page = transactionService.queryPage(queryParams);
        List<TransactionView> list = (List<TransactionView>) page.getList();
        
        if (list == null || list.isEmpty()) {
            return R.error("未找到相关交易记录");
        }
        
        // 一般情况下交易哈希应该是唯一的，所以返回第一条记录
        TransactionView transaction = list.get(0);
        dictionaryService.dictionaryConvert(transaction, request);
        
        return R.ok().put("data", transaction);
    }
} 