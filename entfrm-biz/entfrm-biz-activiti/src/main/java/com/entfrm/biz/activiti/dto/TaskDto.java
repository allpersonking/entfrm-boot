package com.entfrm.biz.activiti.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 任务 dto
 */
@Data
public class TaskDto {
	private String taskId;
	private String taskName;
	private String title;
	private String pdName;
	private String processInstanceId;
	private String status;
	private String nodeKey;
	private String processDefKey;
	private String category;
	private String version;
	private Date createTime;
}
