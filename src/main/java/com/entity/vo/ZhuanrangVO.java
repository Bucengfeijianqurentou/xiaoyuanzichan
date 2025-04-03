package com.entity.vo;

import com.entity.ZhuanrangEntity;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * 资产转让
 * 手机端接口返回实体辅助类
 * （主要作用去除一些不必要的字段）
 */
@TableName("zhuanrang")
public class ZhuanrangVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableField(value = "id")
    private Integer id;

    /**
     * 转让人id
     */
    @TableField(value = "from_id")
    private Integer fromId;

    /**
     * 接收人id
     */
    @TableField(value = "to_id")
    private Integer toId;

    /**
     * 转让时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 资产id
     */
    @TableField(value = "shangpin_id")
    private Integer shangpinId;

    /**
     * 资产名字
     */
    @TableField(value = "shangpin_name")
    private String shangpinName;

    /**
     * 资产价值
     */
    @TableField(value = "shangpin_price")
    private Integer shangpinPrice;

    /**
     * 转让状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 转让方用户名（显示用）
     */
    private String fromUserName;

    /**
     * 接收方用户名（显示用）
     */
    private String toUserName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getShangpinId() {
        return shangpinId;
    }

    public void setShangpinId(Integer shangpinId) {
        this.shangpinId = shangpinId;
    }

    public String getShangpinName() {
        return shangpinName;
    }

    public void setShangpinName(String shangpinName) {
        this.shangpinName = shangpinName;
    }

    public Integer getShangpinPrice() {
        return shangpinPrice;
    }

    public void setShangpinPrice(Integer shangpinPrice) {
        this.shangpinPrice = shangpinPrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
} 