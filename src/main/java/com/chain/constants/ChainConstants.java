package com.chain.constants;

import com.chain.utils.IOUtil;

public class ChainConstants {
    public static final  String URL="http://192.168.124.149:5002";
    //管理员区块俩地址
    public static final  String ADMIN_ADDRESS = "0xd32230e205601544e9ef602f7e664007694cd201";
    //合约地址
    public static final  String ASSET_MANAGEMENT_CONTRACT_ADDRESS = "0x13afdc4f52bc0a5cc635c6a58b77ffe30195be60";
    //交易api
    public static final  String CONTRACT_URL = URL+"/WeBASE-Front/trans/handle";
    //获取交易总数
    public static final  String CHAIN_NUMBER_URL = URL+"/WeBASE-Front/1/web3/transaction-total";
    //获取群组内的共识状态信息
    public static final  String NODE_MANAGE_URL = URL+"/WeBASE-Front/precompiled/consensus/list?groupId=1&pageSize=100&pageNumber=1";
    //获取私钥
    public static final  String PRIVATE_KEY_URL = URL+"/WeBASE-Front/privateKey?type=0&userName=";
    //合约ABI
    public static final String ASSET_MANAGEMENT_ABI = IOUtil.readResourceAsString("abi/AssetManagement.abi");
    //获取块高接口
    public static final String BLOCK_NUMBER = URL + "/WeBASE-Front/1/web3/blockNumber";


}
