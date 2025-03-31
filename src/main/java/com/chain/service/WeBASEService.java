package com.chain.service;

import cn.hutool.core.lang.Dict;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.chain.result.ChainUserVo;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chain.constants.ChainConstants.*;


@Service
public class WeBASEService {
    public ChainUserVo createPrivateKey(Integer type, String userName) {
        ChainUserVo chainUserVo=new ChainUserVo();
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("userName", userName);
        String responseBody = HttpUtil.get(PRIVATE_KEY_URL, params);
        JSONObject bodyJSON = JSONUtil.parseObj(responseBody);
        String address =bodyJSON.getStr("address");
        JSONObject json = new JSONObject();
        json.set("address", bodyJSON.get("address"));
        json.set("publicKey", bodyJSON.get("publicKey"));
        json.set("privateKey ", bodyJSON.get("privateKey"));
        json.set("userName", bodyJSON.get("userName"));
        json.set("type", bodyJSON.get("type"));
        chainUserVo.setHashAddress((String) bodyJSON.get("address"));
        chainUserVo.setPublicKey((String) bodyJSON.get("publicKey"));
        chainUserVo.setPrivateKey((String) bodyJSON.get("privateKey"));
        Dict retDict = new Dict();
        retDict.set("result", json);
        System.out.println(json);
        return chainUserVo;
    }


    /**
     * 注册区块链地址
     * @param username 用户名
     * @return address 账户地址
     */
    public String registerChainCount(String username) {
        String url = PRIVATE_KEY_URL + username;
        String result = HttpUtil.get(url);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        String address = jsonObject.getStr("address");
        return address;
    }


    //创建资产
    public Dict createAsset(List funcParam) {
        return commonReq(ASSET_MANAGEMENT_ABI,ADMIN_ADDRESS,"AssetManagement","createAsset",funcParam,ASSET_MANAGEMENT_CONTRACT_ADDRESS);
    }

    //借用资产
    public Dict borrowAsset(String user,List funcParam) {
        return commonReq(ASSET_MANAGEMENT_ABI,user,"AssetManagement","borrowAsset",funcParam,ASSET_MANAGEMENT_CONTRACT_ADDRESS);
    }


    //归还资产
    public Dict guihuan(String user,List funcParam) {
        return commonReq(ASSET_MANAGEMENT_ABI,ADMIN_ADDRESS,"AssetManagement","guihuan",funcParam,ASSET_MANAGEMENT_CONTRACT_ADDRESS);
    }

    //维修资产
    public Dict weixiu(List funcParam) {
        return commonReq(ASSET_MANAGEMENT_ABI,ADMIN_ADDRESS,"AssetManagement","weixiu",funcParam,ASSET_MANAGEMENT_CONTRACT_ADDRESS);
    }


    //报废资产
    public Dict scrapAsset(List funcParam) {
        return commonReq(ASSET_MANAGEMENT_ABI,ADMIN_ADDRESS,"AssetManagement","scrapAsset",funcParam,ASSET_MANAGEMENT_CONTRACT_ADDRESS);
    }


    //获取块高接口
    public Integer getBlockNumber(){
        String blockNumber = HttpUtil.get(BLOCK_NUMBER);
        return Integer.valueOf(blockNumber);
    }


    public static String generateTransactionHash() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(String.valueOf(System.currentTimeMillis()).getBytes());
        return "0x" + bytesToHex(hashBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        BigInteger bigInt = new BigInteger(1, bytes);
        return bigInt.toString(16);
    }


    private Dict commonReq(String ABI ,String userAddress,String contractName, String funcName, List funcParam,String contractAddress) {
        JSONArray abiJSON = JSONUtil.parseArray(ABI);
        JSONObject data = JSONUtil.createObj();
        data.set("groupId", "1");
        data.set("user", userAddress);
        data.set("contractName", contractName);
        data.set("version", "");
        data.set("funcName", funcName);
        data.set("funcParam", funcParam);
        data.set("contractAddress", contractAddress);
        data.set("contractAbi", abiJSON);
        data.set("useAes", false);
        data.set("useCns", false);
        data.set("cnsName", "");
        String dataString = JSONUtil.toJsonStr(data);
        String responseBody = HttpRequest.post(CONTRACT_URL)
                .header(Header.CONTENT_TYPE, "application/json").body(dataString).execute().body();
        Dict retDict = new Dict();
        retDict.set("result", responseBody);
        return retDict;
    }



}
