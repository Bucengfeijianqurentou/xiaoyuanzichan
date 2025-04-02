package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.beanutils.BeanUtils;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.enums.FieldFill;
import com.baomidou.mybatisplus.enums.IdType;

/**
 * 资产转让
 */
@TableName("zhuanrang")
public class ZhuanrangEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public ZhuanrangEntity() {
    }
    
    public ZhuanrangEntity(T t) {
        try {
            BeanUtils.copyProperties(this, t);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(value = "create_time",fill = FieldFill.INSERT)
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
     * 转让状态 (1: 未接收 2: 已接收 默认值为1)
     */
    @TableField(value = "status")
    private Integer status;

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

    @Override
    public String toString() {
        return "ZhuanrangEntity{" +
            "id=" + id +
            ", fromId=" + fromId +
            ", toId=" + toId +
            ", createTime=" + createTime +
            ", shangpinId=" + shangpinId +
            ", shangpinName='" + shangpinName + '\'' +
            ", shangpinPrice=" + shangpinPrice +
            ", status=" + status +
            '}';
    }
} 