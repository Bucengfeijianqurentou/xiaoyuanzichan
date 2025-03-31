package com.chain.result;


import lombok.Data;

@Data
public class ChainUserVo {

    /**
     * 区块链地址
     */
    private String hashAddress;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;
}
