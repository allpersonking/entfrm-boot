package com.entfrm.core.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 任务状态
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    /**
     * 未提交
     */
    UNSUBMIT("0", "未提交"),

    /**
     * 审核中
     */
    CHECKING("1", "审核中"),

    /**
     * 已完成
     */
    COMPLETED("2", "已完成"),

    /**
     * 驳回
     */
    REJECT("9", "驳回");

    /**
     * 类型
     */
    private final String status;
    /**
     * 描述
     */
    private final String description;
}
