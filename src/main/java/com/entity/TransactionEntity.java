package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.baomidou.mybatisplus.enums.FieldFill;

import java.io.Serializable;
import java.util.Date;

/**
 * 交易记录
 *
 * @author 
 * @email
 */
@TableName("jiaoyi")
public class TransactionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    private Integer id;

    /**
     * 交易哈希
     */
    @TableField(value = "transaction_hash")
    private String transactionHash;

    /**
     * 块高
     */
    @TableField(value = "block_number")
    private Integer blockNumber;

    /**
     * 交易类型
     */
    @TableField(value = "transaction_type")
    private String transactionType;

    /**
     * 交易时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    @TableField(value = "time")
    private Date time;

    /**
     * 发起地址
     */
    @TableField(value = "from")
    private String fromAddress;

    /**
     * 资产ID
     */
    @TableField(value = "assert_id")
    private String assertId;

    /**
     * 操作人
     */
    @TableField(value = "user")
    private String user;

    /**
     * 交易数据
     */
    @TableField(value = "transaction_data")
    private String transactionData;

    /**
     * 获取：主键
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置：主键
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取：交易哈希
     */
    public String getTransactionHash() {
        return transactionHash;
    }

    /**
     * 设置：交易哈希
     */
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    /**
     * 获取：块高
     */
    public Integer getBlockNumber() {
        return blockNumber;
    }

    /**
     * 设置：块高
     */
    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * 获取：交易类型
     */
    public String getTransactionType() {
        return transactionType;
    }

    /**
     * 设置：交易类型
     */
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * 获取：交易时间
     */
    public Date getTime() {
        return time;
    }

    /**
     * 设置：交易时间
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * 获取：发起地址
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * 设置：发起地址
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    /**
     * 获取：资产ID
     */
    public String getAssertId() {
        return assertId;
    }

    /**
     * 设置：资产ID
     */
    public void setAssertId(String assertId) {
        this.assertId = assertId;
    }

    /**
     * 获取：操作人
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置：操作人
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 获取：交易数据
     */
    public String getTransactionData() {
        return transactionData;
    }

    /**
     * 设置：交易数据
     */
    public void setTransactionData(String transactionData) {
        this.transactionData = transactionData;
    }

    @Override
    public String toString() {
        return "TransactionEntity{" +
                "id=" + id +
                ", transactionHash='" + transactionHash + '\'' +
                ", blockNumber=" + blockNumber +
                ", transactionType='" + transactionType + '\'' +
                ", time=" + time +
                ", fromAddress='" + fromAddress + '\'' +
                ", assertId='" + assertId + '\'' +
                ", user='" + user + '\'' +
                ", transactionData='" + transactionData + '\'' +
                '}';
    }
} 