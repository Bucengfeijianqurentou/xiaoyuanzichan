package com.entity.view;

import com.entity.TransactionEntity;
import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 交易记录
 * 后端返回视图实体辅助类
 * （通常后端关联的表或者自定义的字段需要返回使用）
 */
@TableName("jiaoyi")
public class TransactionView extends TransactionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public TransactionView() {

    }

    public TransactionView(TransactionEntity transactionEntity) {
        try {
            BeanUtils.copyProperties(this, transactionEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
} 