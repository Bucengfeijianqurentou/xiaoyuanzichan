package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;

import cn.hutool.core.lang.Dict;
import cn.hutool.json.JSONUtil;
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
 * 校园资产
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/shangpin")
public class ShangpinController {
    private static final Logger logger = LoggerFactory.getLogger(ShangpinController.class);

    @Autowired
    private ShangpinService shangpinService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

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
        params.put("shangpinDeleteStart",1);params.put("shangpinDeleteEnd",1);
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = shangpinService.queryPage(params);

        //字典表数据转换
        List<ShangpinView> list =(List<ShangpinView>)page.getList();
        for(ShangpinView c:list){
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
        ShangpinEntity shangpin = shangpinService.selectById(id);
        if(shangpin !=null){
            //entity转view
            ShangpinView view = new ShangpinView();
            BeanUtils.copyProperties( shangpin , view );//把实体数据重构到view中

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
    public R save(@RequestBody ShangpinEntity shangpin, HttpServletRequest request) throws Exception {
        logger.debug("save方法:,,Controller:{},,shangpin:{}",this.getClass().getName(),shangpin.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");

        Wrapper<ShangpinEntity> queryWrapper = new EntityWrapper<ShangpinEntity>()
            .eq("shangpin_uuid_number", shangpin.getShangpinUuidNumber())
            .eq("shangpin_name", shangpin.getShangpinName())
            .eq("shangpin_types", shangpin.getShangpinTypes())
            .eq("didian_types", shangpin.getDidianTypes())
            .eq("shangpin_kucun_number", shangpin.getShangpinKucunNumber())
            .eq("shangpin_delete", shangpin.getShangpinDelete())
            .eq("price", shangpin.getPrice())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ShangpinEntity shangpinEntity = shangpinService.selectOne(queryWrapper);
        if(shangpinEntity==null){
            shangpin.setCreateTime(new Date());
            shangpin.setShangpinDelete(1);
            String hash = weBASEService.generateTransactionHash();
            shangpin.setTransactionHash(hash);
            shangpinService.insert(shangpin);

            //开始上链
            TransactionEntity transactionEntity = new TransactionEntity();

            Integer blockNumber = weBASEService.getBlockNumber();


            JSONObject jsonObject = new JSONObject();
            jsonObject.put("method","createAsset");
            JSONObject params = new JSONObject();
            params.put("assertId",shangpin.getShangpinUuidNumber());
            params.put("assertName",shangpin.getShangpinName());
            params.put("assertType",shangpin.getShangpinTypes());
            params.put("price",shangpin.getPrice());

            jsonObject.put("params",params);
            String data = jsonObject.toJSONString();
            System.out.println(data);

            transactionEntity.setTransactionHash(hash);
            transactionEntity.setBlockNumber(blockNumber);
            transactionEntity.setTransactionType("资产创建");
            transactionEntity.setTime(new Date());
            transactionEntity.setFromAddress(ADMIN_ADDRESS);
            transactionEntity.setAssertId(shangpin.getShangpinUuidNumber());
            transactionEntity.setUser("管理员");
            transactionEntity.setTransactionData(data);
            transactionService.insert(transactionEntity);


            //TODO 上链
            List<Object> funcParam = new ArrayList<>();
            funcParam.add(Long.parseLong(shangpin.getShangpinUuidNumber()));
            funcParam.add(shangpin.getShangpinName());
            funcParam.add(shangpin.getShangpinTypes());
            funcParam.add(shangpin.getDidianTypes());
            funcParam.add(shangpin.getShangpinKucunNumber());
            Dict dict = weBASEService.createAsset(funcParam);
            System.out.println(dict);

            String resultStr = dict.getStr("result");

            // 将 result 字符串解析为 JSON 对象
            //JSONObject resultJson = JSON.parseObject(resultStr);





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
    public R update(@RequestBody ShangpinEntity shangpin, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,shangpin:{}",this.getClass().getName(),shangpin.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        //根据字段查询是否有相同数据
        Wrapper<ShangpinEntity> queryWrapper = new EntityWrapper<ShangpinEntity>()
            .notIn("id",shangpin.getId())
            .andNew()
            .eq("shangpin_uuid_number", shangpin.getShangpinUuidNumber())
            .eq("shangpin_name", shangpin.getShangpinName())
            .eq("shangpin_types", shangpin.getShangpinTypes())
            .eq("didian_types", shangpin.getDidianTypes())
            .eq("shangpin_kucun_number", shangpin.getShangpinKucunNumber())
            .eq("shangpin_delete", shangpin.getShangpinDelete())
            .eq("price", shangpin.getPrice())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ShangpinEntity shangpinEntity = shangpinService.selectOne(queryWrapper);
        if("".equals(shangpin.getShangpinPhoto()) || "null".equals(shangpin.getShangpinPhoto())){
                shangpin.setShangpinPhoto(null);
        }
        if(shangpinEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      shangpin.set
            //  }
            shangpinService.updateById(shangpin);//根据id更新
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
        shangpinService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 获取总资产数量
     * 查询所有资产库存总和
     */
    @RequestMapping("/getTotalInventory")
    public R getTotalInventory(){
        logger.debug("getTotalInventory方法:,,Controller:{}",this.getClass().getName());
        Integer totalInventory = shangpinService.getTotalInventory();
        // 如果查询结果为空，返回0
        if (totalInventory == null) {
            totalInventory = 0;
        }
        return R.ok().put("data", totalInventory);
    }

    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        try {
            List<ShangpinEntity> shangpinList = new ArrayList<>();//上传的东西
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
                            ShangpinEntity shangpinEntity = new ShangpinEntity();
//                            shangpinEntity.setShangpinUuidNumber(data.get(0));                    //资产编号 要改的
//                            shangpinEntity.setShangpinName(data.get(0));                    //资产名称 要改的
//                            shangpinEntity.setShangpinPhoto("");//照片
//                            shangpinEntity.setShangpinTypes(Integer.valueOf(data.get(0)));   //资产类型 要改的
//                            shangpinEntity.setDidianTypes(Integer.valueOf(data.get(0)));   //存放地点 要改的
//                            shangpinEntity.setShangpinKucunNumber(Integer.valueOf(data.get(0)));   //资产库存 要改的
//                            shangpinEntity.setShangpinDelete(1);//逻辑删除字段
//                            shangpinEntity.setShangpinContent("");//照片
//                            shangpinEntity.setCreateTime(date);//时间
                            shangpinList.add(shangpinEntity);


                            //把要查询是否重复的字段放入map中
                                //资产编号
                                if(seachFields.containsKey("shangpinUuidNumber")){
                                    List<String> shangpinUuidNumber = seachFields.get("shangpinUuidNumber");
                                    shangpinUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> shangpinUuidNumber = new ArrayList<>();
                                    shangpinUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("shangpinUuidNumber",shangpinUuidNumber);
                                }
                        }

                        //查询是否重复
                         //资产编号
                        List<ShangpinEntity> shangpinEntities_shangpinUuidNumber = shangpinService.selectList(new EntityWrapper<ShangpinEntity>().in("shangpin_uuid_number", seachFields.get("shangpinUuidNumber")).eq("shangpin_delete", 1));
                        if(shangpinEntities_shangpinUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(ShangpinEntity s:shangpinEntities_shangpinUuidNumber){
                                repeatFields.add(s.getShangpinUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [资产编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        shangpinService.insertBatch(shangpinList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }


    /**
     * 小程序获取所有资产列表
     */
    @IgnoreAuth
    @RequestMapping("/listAll")
    public R listAll(){
        logger.debug("listAll方法:,,Controller:{}",this.getClass().getName());

        // 构建查询条件
        EntityWrapper<ShangpinEntity> wrapper = new EntityWrapper<ShangpinEntity>();
        wrapper.eq("shangpin_delete", 1); // 只查询未删除的资产

        List<ShangpinEntity> list = shangpinService.selectList(wrapper);
        List<ShangpinView> viewList = new ArrayList<>();

        for(ShangpinEntity entity : list){
            ShangpinView view = new ShangpinView();
            BeanUtils.copyProperties(entity, view);
            viewList.add(view);
        }

        return R.ok().put("data", viewList);
    }





}
