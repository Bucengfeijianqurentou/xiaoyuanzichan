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
 * 资产借用
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/jieyong")
public class JieyongController {
    private static final Logger logger = LoggerFactory.getLogger(JieyongController.class);

    @Autowired
    private JieyongService jieyongService;


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
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = jieyongService.queryPage(params);

        //字典表数据转换
        List<JieyongView> list =(List<JieyongView>)page.getList();
        for(JieyongView c:list){
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
        JieyongEntity jieyong = jieyongService.selectById(id);
        if(jieyong !=null){
            //entity转view
            JieyongView view = new JieyongView();
            BeanUtils.copyProperties( jieyong , view );//把实体数据重构到view中

                //级联表
                ShangpinEntity shangpin = shangpinService.selectById(jieyong.getShangpinId());
                if(shangpin != null){
                    BeanUtils.copyProperties( shangpin , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setShangpinId(shangpin.getId());
                }
                //级联表
                YonghuEntity yonghu = yonghuService.selectById(jieyong.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }


    /**
    * 后端详情
    */
    @RequestMapping("/guihuan/{id}")
    public R guihuan(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("guihuan方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JieyongEntity jieyong = jieyongService.selectById(id);
        if(jieyong !=null){
            if(jieyong.getGuihuanTypes().equals(1)){
                jieyong.setGuihuanTypes(2);


                ShangpinEntity shangpinEntity = shangpinService.selectById(jieyong.getShangpinId());
                if(shangpinEntity != null ){
                    shangpinEntity.setShangpinKucunNumber(shangpinEntity.getShangpinKucunNumber() +jieyong.getJieyongNumber());
                    shangpinService.updateById(shangpinEntity);
                }
                jieyongService.updateById(jieyong);

                YonghuEntity yonghuEntity = yonghuService.selectById(jieyong.getYonghuId());



                //TODO 归还记录上链
                List<Object> funcParam = new ArrayList<>();
                funcParam.add(shangpinEntity.getShangpinUuidNumber());
                Dict dict = weBASEService.guihuan(yonghuEntity.getChainCount(),funcParam);
                System.out.println(dict);

                String resultStr = dict.getStr("result");

                // 将 result 字符串解析为 JSON 对象
                JSONObject resultJson = JSON.parseObject(resultStr);

                // 从 JSON 对象中提取字段
                String transactionHash = resultJson.getString("transactionHash");
                String blockNumber = resultJson.getString("blockNumber");
                String blockHash = resultJson.getString("blockHash");

                System.out.println(transactionHash);
                System.out.println(blockNumber);
                System.out.println(blockHash);

                Map map = new HashMap<>();
                map.put("transactionHash",transactionHash);
                map.put("blockNumber",blockNumber);
                map.put("blockHash",blockHash);

                return R.ok(map);
            }else{
                return R.error(511,"该借出已经归还");
            }
        }else {
            return R.error(511,"查不到借用数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody JieyongEntity jieyong, HttpServletRequest request) throws Exception {
        logger.debug("save方法:,,Controller:{},,jieyong:{}",this.getClass().getName(),jieyong.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            jieyong.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

            jieyong.setInsertTime(new Date());
            jieyong.setCreateTime(new Date());
            jieyong.setGuihuanTypes(3);   //1借用中  2已归还  3审核中

        ShangpinEntity shangpinEntity = shangpinService.selectById(jieyong.getShangpinId());
        if(shangpinEntity == null){
            return R.error(511,"查不到商品");
        }
        int i = shangpinEntity.getShangpinKucunNumber() - jieyong.getJieyongNumber();
        if(i<0){
            return R.error(511,"借用数量大于库存数量");
        }
        shangpinEntity.setShangpinKucunNumber(i);
        String hash = weBASEService.generateTransactionHash();

        shangpinService.updateById(shangpinEntity);
        jieyong.setTransactionHash(hash);
        jieyongService.insert(jieyong);

        YonghuEntity yonghuEntity = yonghuService.selectById(jieyong.getYonghuId());

        //开始上链
        TransactionEntity transactionEntity = new TransactionEntity();

        Integer blockNumber = weBASEService.getBlockNumber();


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method","BorrowAsset");
        JSONObject params = new JSONObject();
        params.put("assertId",shangpinEntity.getShangpinUuidNumber());
        params.put("assertName",shangpinEntity.getShangpinName());
        params.put("assertType",shangpinEntity.getShangpinTypes());
        params.put("guihuan_time",jieyong.getGuihuanTime());

        jsonObject.put("params",params);
        String data = jsonObject.toJSONString();
        System.out.println(data);

        transactionEntity.setTransactionHash(hash);
        transactionEntity.setBlockNumber(blockNumber);
        transactionEntity.setTransactionType("资产借用");
        transactionEntity.setTime(new Date());
        transactionEntity.setFromAddress(yonghuEntity.getChainCount());
        transactionEntity.setAssertId(shangpinEntity.getShangpinUuidNumber());
        transactionEntity.setUser(yonghuEntity.getUsername());
        transactionEntity.setTransactionData(data);
        transactionService.insert(transactionEntity);



        //TODO 借用信息上链
        List<Object> funcParam = new ArrayList<>();
        funcParam.add(Long.parseLong(shangpinEntity.getShangpinUuidNumber()));
        funcParam.add(jieyong.getYonghuId());
        funcParam.add(jieyong.getJieyongNumber());
        funcParam.add(jieyong.getJieyongContent());

        Dict dict = weBASEService.borrowAsset(yonghuEntity.getChainCount(),funcParam);
        System.out.println(dict);

        String resultStr = dict.getStr("result");

        // 将 result 字符串解析为 JSON 对象
        JSONObject resultJson = JSON.parseObject(resultStr);

        // 从 JSON 对象中提取字段

        Map map = new HashMap<>();
        map.put("transactionHash",hash);
        return R.ok(map);
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody JieyongEntity jieyong, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,jieyong:{}",this.getClass().getName(),jieyong.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            jieyong.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<JieyongEntity> queryWrapper = new EntityWrapper<JieyongEntity>()
            .notIn("id",jieyong.getId())
            .andNew()
            .eq("yonghu_id", jieyong.getYonghuId())
            .eq("shangpin_id", jieyong.getShangpinId())
            .eq("jieyong_number", jieyong.getJieyongNumber())
            .eq("guihuan_types", jieyong.getGuihuanTypes())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JieyongEntity jieyongEntity = jieyongService.selectOne(queryWrapper);
        if(jieyongEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      jieyong.set
            //  }
            jieyongService.updateById(jieyong);//根据id更新
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
        logger.debug("delete方法:,,Controller:{},,ids:{}",this.getClass().getName(),ids);
        jieyongService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 批准借用申请
     */
    @RequestMapping("/approve/{id}")
    public R approve(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("approve方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JieyongEntity jieyong = jieyongService.selectById(id);
        if(jieyong == null){
            return R.error("借用记录不存在");
        }
        
        if(!jieyong.getGuihuanTypes().equals(3)){
            return R.error("只能批准审核中的借用申请");
        }
        
        // 更新状态为借用中
        jieyong.setGuihuanTypes(1);
        jieyongService.updateById(jieyong);
        
        // 获取相关实体
        ShangpinEntity shangpinEntity = shangpinService.selectById(jieyong.getShangpinId());
        YonghuEntity yonghuEntity = yonghuService.selectById(jieyong.getYonghuId());
        
        // 更新资产库存
        if(shangpinEntity != null){
            // 检查库存是否足够
            if(shangpinEntity.getShangpinKucunNumber() < jieyong.getJieyongNumber()){
                return R.error("资产库存不足，无法批准借用");
            }
            
            // 减少库存
            shangpinEntity.setShangpinKucunNumber(shangpinEntity.getShangpinKucunNumber() - jieyong.getJieyongNumber());
            shangpinService.updateById(shangpinEntity);
        }
        
        // 借用记录上链
        if(yonghuEntity != null && shangpinEntity != null){
            try {
                List<Object> funcParam = new ArrayList<>();

                //开始上链
                TransactionEntity transactionEntity = new TransactionEntity();
                String hash = weBASEService.generateTransactionHash();

                Integer blockNumber = weBASEService.getBlockNumber();


                JSONObject jsonObject = new JSONObject();
                jsonObject.put("method","BorrowAsset");
                JSONObject params = new JSONObject();
                params.put("assertId",shangpinEntity.getShangpinUuidNumber());
                params.put("assertName",shangpinEntity.getShangpinName());
                params.put("assertType",shangpinEntity.getShangpinTypes());
                params.put("isApprove","批准");

                jsonObject.put("params",params);
                String data = jsonObject.toJSONString();
                System.out.println(data);

                transactionEntity.setTransactionHash(hash);
                transactionEntity.setBlockNumber(blockNumber);
                transactionEntity.setTransactionType("资产借用");
                transactionEntity.setTime(new Date());
                transactionEntity.setFromAddress(ADMIN_ADDRESS);
                transactionEntity.setAssertId(shangpinEntity.getShangpinUuidNumber());
                transactionEntity.setUser("管理员");
                transactionEntity.setTransactionData(data);
                transactionService.insert(transactionEntity);

                // 更新借用记录的交易哈希
                jieyong.setTransactionHash(hash);
                jieyongService.updateById(jieyong);
            } catch (Exception e) {
                logger.error("借用上链失败", e);
                // 上链失败不影响业务流程
            }
        }
        
        return R.ok();
    }
    
    /**
     * 拒绝借用申请
     */
    @RequestMapping("/reject/{id}")
    public R reject(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("reject方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JieyongEntity jieyong = jieyongService.selectById(id);
        if(jieyong == null){
            return R.error("借用记录不存在");
        }
        
        if(!jieyong.getGuihuanTypes().equals(3)){
            return R.error("只能拒绝审核中的借用申请");
        }
        
        // 删除借用记录
        jieyongService.deleteById(id);
        
        return R.ok();
    }

    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        try {
            List<JieyongEntity> jieyongList = new ArrayList<>();//上传的东西
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
                            JieyongEntity jieyongEntity = new JieyongEntity();
//                            jieyongEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            jieyongEntity.setShangpinId(Integer.valueOf(data.get(0)));   //校园资产 要改的
//                            jieyongEntity.setJieyongNumber(Integer.valueOf(data.get(0)));   //借用数量 要改的
//                            jieyongEntity.setJieyongContent("");//照片
//                            jieyongEntity.setGuihuanTypes(Integer.valueOf(data.get(0)));   //是否归还 要改的
//                            jieyongEntity.setInsertTime(date);//时间
//                            jieyongEntity.setCreateTime(date);//时间
                            jieyongList.add(jieyongEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        jieyongService.insertBatch(jieyongList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }

    /**
     * 小程序专用借用保存接口
     */
    @IgnoreAuth
    @RequestMapping("/wxSave")
    public R wxSave(@RequestBody JieyongEntity jieyong){
        logger.debug("wxSave方法:,,Controller:{},,jieyong:{}",this.getClass().getName(),JSONObject.toJSONString(jieyong));
        
        // 验证资产是否存在且有库存
        ShangpinEntity shangpin = shangpinService.selectById(jieyong.getShangpinId());
        if(shangpin == null){
            return R.error(511,"借用的资产不存在");
        }
        if(shangpin.getShangpinKucunNumber() <= 0){
            return R.error(511,"该资产已无库存");
        }
        
        // 设置借用状态为待审核
        jieyong.setGuihuanTypes(3); // 3表示待审核
        
        // 设置创建时间
        jieyong.setCreateTime(new Date());
        
        // 保存借用信息
        jieyongService.insert(jieyong);
        
        return R.ok();
    }

}
