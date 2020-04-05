package com.entfrm.biz.activiti.dto;

import lombok.Data;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 流程 dto
 */
@Data
public class ProcessDto {
    private String category;
    private String processonDefinitionId;
    private String key;
    private String name;
    private Integer version;
    private Long deploymentTime;
    private String xmlName;
    private String picName;
    private String deploymentId;
    private Boolean suspend;
    private String description;
    private Integer xAxis;
    private Integer yAxis;
    private Integer width;
    private Integer height;

    /**
     * 流程定义内容转换
     *
     * @param processDefinition
     * @param deployment
     * @return
     */
    public static ProcessDto toProcessDto(ProcessDefinition processDefinition, Deployment deployment) {
        ProcessDto dto = new ProcessDto();
        dto.setCategory(processDefinition.getCategory());
        dto.setProcessonDefinitionId(processDefinition.getId());
        dto.setKey(processDefinition.getKey());
        dto.setName(deployment.getName());
        dto.setVersion(processDefinition.getVersion());
        dto.setDeploymentTime(deployment.getDeploymentTime().getTime());
        dto.setXmlName(processDefinition.getResourceName());
        dto.setPicName(processDefinition.getDiagramResourceName());
        dto.setDeploymentId(deployment.getId());
        dto.setSuspend(processDefinition.isSuspended());
        dto.setDescription(processDefinition.getDescription());
        return dto;
    }
}
