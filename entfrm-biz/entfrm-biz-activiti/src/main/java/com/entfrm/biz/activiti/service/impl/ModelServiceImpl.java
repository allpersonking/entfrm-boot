package com.entfrm.biz.activiti.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.entfrm.biz.activiti.service.ModelService;
import com.entfrm.core.base.constant.CommonConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @author entfrm
 * @date 2020/3/24
 * @description 模型 实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class ModelServiceImpl implements ModelService {
    private static final String BPMN20_XML = ".bpmn20.xml";
    private final RepositoryService repositoryService;
    private final ObjectMapper objectMapper;

    /**
     * 流程分页查询
     *
     * @param params
     * @return
     */
    @Override
    public IPage<Model> list(Map<String, Object> params) {
        ModelQuery modelQuery = repositoryService.createModelQuery()
                .latestVersion().orderByLastUpdateTime().desc();

        String name = (String) params.get("name");
        if (StrUtil.isNotBlank(name)) {
            modelQuery.modelNameLike(name);
        }

        String category = (String) params.get("category");
        if (StrUtil.isNotBlank(category)) {
            modelQuery.modelCategory(category);
        }

        int current = MapUtil.getInt(params, CommonConstants.CURRENT);
        int size = MapUtil.getInt(params, CommonConstants.SIZE);

        IPage result = new Page(current, size);
        result.setTotal(modelQuery.count());
        result.setRecords(modelQuery.listPage((current - 1) * size, size));
        return result;
    }

    /**
     * 流程新增
     *
     * @param name
     * @param category
     * @param key
     * @param description
     * @return
     */
    @Override
    public Model save(String name, String category, String key, String description) {
        try {
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id" , "canvas");
            editorNode.put("resourceId" , "canvas");
            ObjectNode properties = objectMapper.createObjectNode();
            properties.put("process_author" , CommonConstants.AUTHOR);
            properties.put("process_id" , key);
            properties.put("name" , name);
            editorNode.set("properties" , properties);
            ObjectNode stencilset = objectMapper.createObjectNode();
            stencilset.put("namespace" , "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.set("stencilset" , stencilset);

            Model model = repositoryService.newModel();
            model.setKey(key);
            model.setName(name);
            model.setCategory(category);
            model.setVersion(Integer.parseInt(
                    String.valueOf(repositoryService.createModelQuery()
                            .modelKey(model.getKey()).count() + 1)));

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getVersion());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            model.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(model);
            repositoryService.addModelEditorSource(model.getId(), editorNode.toString().getBytes("utf-8"));
            return model;
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException" , e);
        }
        return null;
    }

    /**
     * 流程删除
     *
     * @param id
     * @return
     */
    @Override
    public Boolean removeById(String id) {
        repositoryService.deleteModel(id);
        return Boolean.TRUE;
    }

    /**
     * 流程部署
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deploy(String id) {
        try {
            // 获取模型
            Model model = repositoryService.getModel(id);
            ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(model.getId()));
            BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(objectNode);

            String processName = model.getName();
            if (!StrUtil.endWithIgnoreCase(processName, BPMN20_XML)) {
                processName += BPMN20_XML;
            }
            // 部署流程
            Deployment deployment = repositoryService
                    .createDeployment().name(model.getName())
                    .addBpmnModel(processName, bpmnModel)
                    .deploy();

            // 设置流程分类
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .list();

            list.stream().forEach(processDefinition ->
                    repositoryService.setProcessDefinitionCategory(processDefinition.getId(), model.getCategory()));
        } catch (Exception e) {
            log.error("部署失败，异常" , e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
