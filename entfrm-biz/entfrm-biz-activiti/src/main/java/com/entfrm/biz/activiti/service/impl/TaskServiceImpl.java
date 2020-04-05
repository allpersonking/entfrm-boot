package com.entfrm.biz.activiti.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.entfrm.biz.activiti.dto.CommentDto;
import com.entfrm.biz.activiti.dto.LeaveDto;
import com.entfrm.biz.activiti.dto.TaskDto;
import com.entfrm.biz.activiti.entity.Leave;
import com.entfrm.biz.activiti.service.LeaveService;
import com.entfrm.core.base.constant.CommonConstants;
import com.entfrm.core.base.enums.TaskStatusEnum;
import com.entfrm.core.security.util.SecurityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 流程 service
 */
@Slf4j
@Service
@AllArgsConstructor
public class TaskServiceImpl implements com.entfrm.biz.activiti.service.TaskService {
    private static final String FLAG = "审批";
    private final LeaveService leaveService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final ProcessEngineFactoryBean processEngine;

    @Override
    public IPage list(Map<String, Object> params) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskCandidateOrAssigned(SecurityUtil.getUser().getUsername());

        String taskName = (String) params.get("taskName");
        if (StrUtil.isNotBlank(taskName)) {
            taskQuery.taskNameLike(taskName);
        }

        int page = MapUtil.getInt(params, CommonConstants.CURRENT);
        int limit = MapUtil.getInt(params, CommonConstants.SIZE);

        IPage result = new Page(page, limit);
        result.setTotal(taskQuery.count());

        List<TaskDto> taskDTOList = taskQuery.orderByTaskCreateTime().desc()
                .listPage((page - 1) * limit, limit).stream().map(task -> {
                    TaskDto dto = new TaskDto();
                    dto.setTaskId(task.getId());
                    dto.setTaskName(task.getName());
                    dto.setProcessInstanceId(task.getProcessInstanceId());
                    dto.setNodeKey(task.getTaskDefinitionKey());
                    dto.setCategory(task.getCategory());
                    dto.setStatus(task.isSuspended() ? "0" : "1");
                    dto.setCreateTime(task.getCreateTime());
                    return dto;
                }).collect(Collectors.toList());
        result.setRecords(taskDTOList);
        return result;
    }

    /**
     * 通过任务ID查询任务信息
     *
     * @param taskId
     * @return
     */
    @Override
    public LeaveDto getTaskById(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();

        String businessKey = pi.getBusinessKey();
        if (StrUtil.isNotBlank(businessKey)) {
            businessKey = businessKey.split("_")[1];
        }

        List<String> comeList = findOutFlagListByTaskId(task, pi);
        Leave leave = leaveService.getById(businessKey);

        LeaveDto leaveDto = new LeaveDto();
        BeanUtils.copyProperties(leave, leaveDto);
        leaveDto.setTaskId(taskId);
        leaveDto.setTaskName(task.getName());
        leaveDto.setTime(task.getCreateTime());
        leaveDto.setFlagList(comeList);
        return leaveDto;
    }

    /**
     * 提交任务
     *
     * @param leaveDto
     * @return
     */
    @Override
    public Boolean checkTask(LeaveDto leaveDto) {
        String taskId = leaveDto.getTaskId();
        String message = leaveDto.getComment();
        Integer id = leaveDto.getId();

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        String processInstanceId = task.getProcessInstanceId();
        Authentication.setAuthenticatedUserId(SecurityUtil.getUser().getUsername());
        taskService.addComment(taskId, processInstanceId, message);

        Map<String, Object> variables = new HashMap<>(1);
        variables.put("flag" , leaveDto.getTaskFlag());
        variables.put("type" , leaveDto.getType());
        variables.put("days" , leaveDto.getDays());

        taskService.complete(taskId, variables);
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (pi == null) {
            Leave bill = new Leave();
            bill.setId(id);
            bill.setStatus(StrUtil.equals(TaskStatusEnum.REJECT.getDescription()
                    , leaveDto.getTaskFlag()) ? TaskStatusEnum.REJECT.getStatus()
                    : TaskStatusEnum.COMPLETED.getStatus());
            leaveService.updateById(bill);
        }
        return null;
    }

    @Override
    public List<CommentDto> commitList(String taskId) {
        //使用当前任务ID，获取当前任务对象
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        //获取流程实例ID
        List<CommentDto> commentDtoList = taskService
                .getProcessInstanceComments(task.getProcessInstanceId())
                .stream().map(comment -> {
                            CommentDto commentDto = new CommentDto();
                            commentDto.setId(comment.getId());
                            commentDto.setTime(comment.getTime());
                            commentDto.setType(comment.getType());
                            commentDto.setTaskId(comment.getTaskId());
                            commentDto.setUserId(comment.getUserId());
                            commentDto.setFullMessage(comment.getFullMessage());
                            commentDto.setProcessInstanceId(comment.getProcessInstanceId());
                            return commentDto;
                        }
                ).collect(Collectors.toList());
        return commentDtoList;
    }

    /**
     * 追踪图片节点
     *
     * @param id
     */
    @Override
    public InputStream trackImage(String id) {
        //使用当前任务ID，获取当前任务对象
        Task task = taskService.createTaskQuery()
                .taskId(id)
                .singleResult();

        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        HistoricProcessInstance historicProcessInstance =
                historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = null;
        List<String> executedActivityIdList = new ArrayList<>();
        if (processInstance != null) {
            processDefinitionId = processInstance.getProcessDefinitionId();
            executedActivityIdList = this.runtimeService.getActiveActivityIds(processInstance.getId());
        } else if (historicProcessInstance != null) {
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            executedActivityIdList = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderByHistoricActivityInstanceId().asc().list()
                    .stream().map(HistoricActivityInstance::getActivityId)
                    .collect(Collectors.toList());
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();

        return diagramGenerator.generateDiagram(
                bpmnModel, "png" ,
                executedActivityIdList, Collections.emptyList(),
                processEngine.getProcessEngineConfiguration().getActivityFontName(),
                processEngine.getProcessEngineConfiguration().getLabelFontName(),
                "宋体" ,
                null, 1.0);

    }

    private List<String> findOutFlagListByTaskId(Task task, ProcessInstance pi) {
        //查询ProcessDefinitionEntiy对象
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService
                .getProcessDefinition(task.getProcessDefinitionId());

        ActivityImpl activityImpl = processDefinitionEntity.findActivity(pi.getActivityId());
        //获取当前活动完成之后连线的名称
        List<String> nameList = activityImpl.getOutgoingTransitions().stream()
                .map(pvm -> {
                    String name = (String) pvm.getProperty("name");
                    return StrUtil.isNotBlank(name) ? name : FLAG;
                }).collect(Collectors.toList());
        return nameList;
    }
}
