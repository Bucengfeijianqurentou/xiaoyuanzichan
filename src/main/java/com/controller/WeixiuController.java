package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;

import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSONObject;
import java.util.*;

import com.chain.service.WeBASEService;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

import static com.chain.constants.ChainConstants.ADMIN_ADDRESS;

/**
 * 资产维修
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/weixiu")
public class WeixiuController {
    private static final Logger logger = LoggerFactory.getLogger(WeixiuController.class);

    @Autowired
    private WeixiuService weixiuService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private ShangpinService shangpinService;

    @Autowired
    private YonghuService yonghuService;

    @Autowired
    private WeBASEService weBASEService;

    @Autowired
    private TransactionService transactionService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        // 取消用户角色限制，让用户也能看到所有维修记录
        // else if("用户".equals(role))
        //     params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = weixiuService.queryPage(params);

        //字典表数据转换
        List<WeixiuView> list =(List<WeixiuView>)page.getList();
        for(WeixiuView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        WeixiuEntity weixiu = weixiuService.selectById(id);
        if(weixiu !=null){
            //entity转view
            WeixiuView view = new WeixiuView();
            BeanUtils.copyProperties( weixiu , view );//把实体数据重构到view中

                //级联表
                ShangpinEntity shangpin = shangpinService.selectById(weixiu.getShangpinId());
                if(shangpin != null){
                    BeanUtils.copyProperties( shangpin , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setShangpinId(shangpin.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody WeixiuEntity weixiu, HttpServletRequest request) throws Exception {
        logger.debug("save方法:,,Controller:{},,weixiu:{}",this.getClass().getName(),weixiu.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        
        // 获取当前登录用户ID
        Integer userId = null;
        if ("用户".equals(role)) {
            userId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
            // 设置申请人
            weixiu.setYonghuId(userId);
            // 设置审核状态为待审核
            weixiu.setShenheStatus(0);
            // 设置维修状态为未开始
            weixiu.setStatus(0);
        } else if ("管理员".equals(role)) {
            // 管理员直接创建的维修记录不需要审核
            weixiu.setShenheStatus(1); // 已批准
        }

        Wrapper<WeixiuEntity> queryWrapper = new EntityWrapper<WeixiuEntity>()
            .eq("shangpin_id", weixiu.getShangpinId())
            .eq("weixiu_number", weixiu.getWeixiuNumber())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WeixiuEntity weixiuEntity = weixiuService.selectOne(queryWrapper);
        if(weixiuEntity==null){
            weixiu.setInsertTime(new Date());
            weixiu.setCreateTime(new Date());
            String hash = weBASEService.generateTransactionHash();
            weixiu.setTransactionHash(hash);
            weixiuService.insert(weixiu);

            ShangpinEntity shangpinEntity = shangpinService.selectById(weixiu.getShangpinId());

            // 如果是管理员，或者是已批准的申请，则执行上链操作
            if ("管理员".equals(role) || weixiu.getShenheStatus() == 1) {
                //开始上链
                TransactionEntity transactionEntity = new TransactionEntity();

                Integer blockNumber = weBASEService.getBlockNumber();

                System.out.println(hash);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("method","BorrowAsset");
                JSONObject params = new JSONObject();
                params.put("assertId",weixiu.getShangpinId());
                params.put("assertName",shangpinEntity.getShangpinName());
                params.put("assertType",shangpinEntity.getShangpinTypes());
                params.put("weixiuNumber",weixiu.getWeixiuNumber());
                params.put("weixiuContent",weixiu.getWeixiuContent());


                jsonObject.put("params",params);
                String data = jsonObject.toJSONString();
                System.out.println(data);

                transactionEntity.setTransactionHash(hash);
                transactionEntity.setBlockNumber(blockNumber);
                transactionEntity.setTransactionType("资产维修");
                transactionEntity.setTime(new Date());
                transactionEntity.setFromAddress(ADMIN_ADDRESS);
                transactionEntity.setAssertId(shangpinEntity.getShangpinUuidNumber());
                transactionEntity.setUser("管理员".equals(role) ? "管理员" : "用户");
                transactionEntity.setTransactionData(data);
                transactionService.insert(transactionEntity);


                //维修数据上链
                List<Object> funcParam = new ArrayList<>();
                funcParam.add(weixiu.getShangpinId());
                funcParam.add(weixiu.getWeixiuNumber());
                funcParam.add(weixiu.getWeixiuContent());
                Dict dict = weBASEService.weixiu(funcParam);

                String resultStr = dict.getStr("result");

                // 将 result 字符串解析为 JSON 对象
                JSONObject resultJson = JSON.parseObject(resultStr);
            }

            Map map = new HashMap<>();
            map.put("transactionHash",hash);

            return R.ok(map);
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody WeixiuEntity weixiu, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,weixiu:{}",this.getClass().getName(),weixiu.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        //根据字段查询是否有相同数据
        Wrapper<WeixiuEntity> queryWrapper = new EntityWrapper<WeixiuEntity>()
            .notIn("id",weixiu.getId())
            .andNew()
            .eq("shangpin_id", weixiu.getShangpinId())
            .eq("weixiu_number", weixiu.getWeixiuNumber())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WeixiuEntity weixiuEntity = weixiuService.selectOne(queryWrapper);
        if(weixiuEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      weixiu.set
            //  }
            weixiuService.updateById(weixiu);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        weixiuService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        try {
            List<WeixiuEntity> weixiuList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            WeixiuEntity weixiuEntity = new WeixiuEntity();
//                            weixiuEntity.setShangpinId(Integer.valueOf(data.get(0)));   //校园资产 要改的
//                            weixiuEntity.setWeixiuNumber(Integer.valueOf(data.get(0)));   //维修数量 要改的
//                            weixiuEntity.setWeixiuContent("");//照片
//                            weixiuEntity.setInsertTime(date);//时间
//                            weixiuEntity.setCreateTime(date);//时间
                            weixiuList.add(weixiuEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        weixiuService.insertBatch(weixiuList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }

    /**
     * 审核维修申请
     */
    @RequestMapping("/shenhe")
    public R shenhe(@RequestBody Map<String, Object> params, HttpServletRequest request) throws Exception {
        logger.debug("shenhe方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
            
        // 只有管理员能审核
        if(!"管理员".equals(role)){
            return R.error(511, "只有管理员能审核维修申请");
        }
        
        // 获取请求参数
        Integer id = Integer.valueOf(String.valueOf(params.get("id")));
        Integer shenheStatus = Integer.valueOf(String.valueOf(params.get("shenheStatus")));
        String shenheNote = params.get("shenheNote") != null ? String.valueOf(params.get("shenheNote")) : "";
        
        if(id == null || shenheStatus == null){
            return R.error(511,"参数不完整");
        }
        
        // 获取原实体
        WeixiuEntity weixiu = weixiuService.selectById(id);
        if(weixiu == null){
            return R.error(511,"找不到对应的维修申请");
        }
        
        // 只能审核待审核的申请
        if(weixiu.getShenheStatus() != 0){
            return R.error(511,"该申请已经被审核过了");
        }
        
        // 设置审核状态
        weixiu.setShenheStatus(shenheStatus);
        
        // 如果批准申请，更新维修状态
        if(shenheStatus == 1){
            weixiu.setStatus(0); // 设为未开始状态
            
            // 获取关联的资产信息
            ShangpinEntity shangpinEntity = shangpinService.selectById(weixiu.getShangpinId());
            if(shangpinEntity != null){
                // 上链操作
                String hash = weBASEService.generateTransactionHash();
                weixiu.setTransactionHash(hash);
                
                TransactionEntity transactionEntity = new TransactionEntity();
                Integer blockNumber = weBASEService.getBlockNumber();
                
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("method", "RepairAsset");
                JSONObject params2 = new JSONObject();
                params2.put("assertId", weixiu.getShangpinId());
                params2.put("assertName", shangpinEntity.getShangpinName());
                params2.put("assertType", shangpinEntity.getShangpinTypes());
                params2.put("weixiuNumber", weixiu.getWeixiuNumber());
                params2.put("weixiuContent", weixiu.getWeixiuContent());
                params.put("yonghuId",weixiu.getYonghuId());
                params.put("shenheStatus",weixiu.getShenheStatus());
                params2.put("shenheStatus", "批准");
                
                jsonObject.put("params", params2);
                String data = jsonObject.toJSONString();
                
                transactionEntity.setTransactionHash(hash);
                transactionEntity.setBlockNumber(blockNumber);
                transactionEntity.setTransactionType("维修申请审核");
                transactionEntity.setTime(new Date());
                transactionEntity.setFromAddress(ADMIN_ADDRESS);
                transactionEntity.setAssertId(shangpinEntity.getShangpinUuidNumber());
                transactionEntity.setUser("管理员");
                transactionEntity.setTransactionData(data);
                transactionService.insert(transactionEntity);
                
                // 维修数据上链
                List<Object> funcParam = new ArrayList<>();
                funcParam.add(weixiu.getShangpinId());
                funcParam.add(weixiu.getWeixiuNumber());
                funcParam.add(weixiu.getWeixiuContent());
                Dict dict = weBASEService.weixiu(funcParam);
            }
        } else if(shenheStatus == 2){
            // 如果拒绝申请，不执行上链操作
        }
        
        // 更新实体
        weixiuService.updateById(weixiu);
        
        return R.ok();
    }

    /**
    * 更新维修状态
    */
    @RequestMapping("/updateStatus")
    public R updateStatus(@RequestBody Map<String, Object> params, HttpServletRequest request){
        logger.debug("updateStatus方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
            
        // 获取请求参数
        Integer id = Integer.valueOf(String.valueOf(params.get("id")));
        Integer status = Integer.valueOf(String.valueOf(params.get("status")));
        
        if(id == null || status == null){
            return R.error(511,"参数不完整");
        }
        
        // 获取原实体
        WeixiuEntity weixiu = weixiuService.selectById(id);
        if(weixiu == null){
            return R.error(511,"找不到对应的维修记录");
        }
        
        // 只更新状态字段
        weixiu.setStatus(status);
        weixiuService.updateById(weixiu);
        
        return R.ok();
    }





}
