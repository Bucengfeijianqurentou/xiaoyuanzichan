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
 * 资产维修
 *
 * @author 
 * @email
 */
@TableName("weixiu")
public class WeixiuEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;


	public WeixiuEntity() {

	}

	public WeixiuEntity(T t) {
		try {
			BeanUtils.copyProperties(this, t);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
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
     * 校园资产
     */
    @TableField(value = "shangpin_id")

    private Integer shangpinId;


    /**
     * 维修数量
     */
    @TableField(value = "weixiu_number")

    private Integer weixiuNumber;


    /**
     * 维修备注
     */
    @TableField(value = "weixiu_content")

    private String weixiuContent;


    /**
     * 维修时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
    @TableField(value = "insert_time",fill = FieldFill.INSERT)

    private Date insertTime;


    /**
     * 创建时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
    @TableField(value = "create_time",fill = FieldFill.INSERT)

    private Date createTime;


    /**
     * 交易哈希
     */
    @TableField(value = "transaction_hash")
    private String transactionHash;

    /**
     * 维修状态（0：未开始，1：维修中，2：已完成）
     */
    @TableField(value = "status")
    private Integer status;
    
    /**
     * 用户ID
     */
    @TableField(value = "yonghu_id")
    private Integer yonghuId;
    
    /**
     * 审核状态（0：待审核，1：已批准，2：已拒绝）
     */
    @TableField(value = "shenhe_status")
    private Integer shenheStatus;


    /**
	 * 设置：主键
	 */
    public Integer getId() {
        return id;
    }


    /**
	 * 获取：主键
	 */

    public void setId(Integer id) {
        this.id = id;
    }
    /**
	 * 设置：校园资产
	 */
    public Integer getShangpinId() {
        return shangpinId;
    }


    /**
	 * 获取：校园资产
	 */

    public void setShangpinId(Integer shangpinId) {
        this.shangpinId = shangpinId;
    }
    /**
	 * 设置：维修数量
	 */
    public Integer getWeixiuNumber() {
        return weixiuNumber;
    }


    /**
	 * 获取：维修数量
	 */

    public void setWeixiuNumber(Integer weixiuNumber) {
        this.weixiuNumber = weixiuNumber;
    }
    /**
	 * 设置：维修备注
	 */
    public String getWeixiuContent() {
        return weixiuContent;
    }


    /**
	 * 获取：维修备注
	 */

    public void setWeixiuContent(String weixiuContent) {
        this.weixiuContent = weixiuContent;
    }
    /**
	 * 设置：维修时间
	 */
    public Date getInsertTime() {
        return insertTime;
    }


    /**
	 * 获取：维修时间
	 */

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
    /**
	 * 设置：创建时间
	 */
    public Date getCreateTime() {
        return createTime;
    }


    /**
	 * 获取：创建时间
	 */

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
     * 获取：维修状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置：维修状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取：用户ID
     */
    public Integer getYonghuId() {
        return yonghuId;
    }

    /**
     * 设置：用户ID
     */
    public void setYonghuId(Integer yonghuId) {
        this.yonghuId = yonghuId;
    }

    /**
     * 获取：审核状态
     */
    public Integer getShenheStatus() {
        return shenheStatus;
    }

    /**
     * 设置：审核状态
     */
    public void setShenheStatus(Integer shenheStatus) {
        this.shenheStatus = shenheStatus;
    }

    @Override
    public String toString() {
        return "Weixiu{" +
            "id=" + id +
            ", shangpinId=" + shangpinId +
            ", weixiuNumber=" + weixiuNumber +
            ", weixiuContent=" + weixiuContent +
            ", insertTime=" + insertTime +
            ", createTime=" + createTime +
            ", transactionHash=" + transactionHash +
            ", status=" + status +
            ", yonghuId=" + yonghuId +
            ", shenheStatus=" + shenheStatus +
        "}";
    }
}
