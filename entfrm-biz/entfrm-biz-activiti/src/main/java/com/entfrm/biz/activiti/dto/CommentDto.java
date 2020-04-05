package com.entfrm.biz.activiti.dto;

import lombok.Data;
import org.activiti.engine.task.Comment;

import java.util.Date;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 审批意见 dto
 */
@Data
public class CommentDto {

    private String id;
    private String userId;
    private Date time;
    private String taskId;
    private String processInstanceId;
    private String type;
    private String fullMessage;

    //其他信息

}
