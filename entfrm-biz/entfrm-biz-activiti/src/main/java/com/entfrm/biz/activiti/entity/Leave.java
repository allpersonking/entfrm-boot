package com.entfrm.biz.activiti.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.entfrm.core.base.annotation.Excel;
import com.entfrm.core.data.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 请假 entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("act_leave")
public class Leave extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 申请人
     */
    @Excel(name = "申请人")
    private String userName;
    /**
     * 类型
     */
    @Excel(name = "请假类型", convertExp = "0=事假,1=病假,2=产假")
    private String type;
    /**
     * 事由
     */
    @Excel(name = "请假事由")
    private String cause;
    /**
     * 请假时间
     */
    @Excel(name = "请假时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaveTime;
    /**
     * 天数
     */
    @Excel(name = "天数")
    private Integer days;
    /**
     * 0=未提交,1=未审核,2=批准,9=驳回
     */
    @Excel(name = "状态", convertExp = "0=未提交,1=未审核,2=批准,9=驳回")
    private String status;

}
