package com.chain.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chain.result.AjaxResult;
import com.chain.service.WeBASEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chain.constants.ChainConstants.*;


@RestController
@RequestMapping("/chain")
public class ChainController {
    @Autowired
    WeBASEService service;

    /*@GetMapping("/getchain")
    public Object getBlockNumber() {
        Map<String, Object> data = new HashMap<>();
        String chainNumber = HttpUtil.get(CHAIN_NUMBER_URL);
        String s = HttpUtil.get(NODE_MANAGE_URL);
        JSONObject jsonObject1 = new JSONObject(chainNumber);
        int blockNumber = jsonObject1.getInt("blockNumber");
        int txSum=jsonObject1.getInt("txSum");


        JSONObject jsonObject = new JSONObject(s);
        int nodeCount = jsonObject.getInt("totalCount");
        JSONArray dataArray = jsonObject.getJSONArray("data");

        List<String> nodeIds = new ArrayList<>();
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            String nodeId = dataObject.getStr("nodeId");
            nodeIds.add(nodeId);
        }

        data.put("nodeIds", nodeIds);
        data.put("blockNumber", blockNumber);
        data.put("txSum", txSum);
        data.put("nodeCount", nodeCount);

        return AjaxResult.success(data);
    }*/

    @GetMapping("/getTransactionTotal")
    public Integer getTransactionTotal(){
        String res = HttpUtil.get(CHAIN_NUMBER_URL);
        JSONObject obj = JSONUtil.parseObj(res);
        return obj.getInt("txSum");
    }

    @GetMapping("/getBlockNumber")
    public Integer getBlockNumber(){
        String res = HttpUtil.get(BLOCK_NUMBER);
        return Integer.valueOf(res);
    }

}
