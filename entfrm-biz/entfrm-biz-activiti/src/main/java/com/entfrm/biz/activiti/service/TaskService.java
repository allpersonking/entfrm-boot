package com.entfrm.biz.activiti.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.entfrm.biz.activiti.dto.CommentDto;
import com.entfrm.biz.activiti.dto.LeaveDto;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 任务 service
 */
public interface TaskService {
    /**
     * 获取任务列表
     *
     * @param params
     * @return
     */
    IPage list(Map<String, Object> params);

    /**
     * 通过任务ID查询任务信息关联申请单信息
     *
     * @param id
     * @return
     */
    LeaveDto getTaskById(String id);

    /**
     * 审批任务
     *
     * @param leaveDto
     * @return
     */
    Boolean checkTask(LeaveDto leaveDto);

    /**
     * 通过任务ID 查询审批意见信息
     *
     * @param taskId 任务ID
     * @return
     */
    List<CommentDto> commitList(String taskId);

    /**
     * 追踪图片节点
     *
     * @param id
     * @return
     */
    InputStream trackImage(String id);
}
