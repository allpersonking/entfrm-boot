package com.entfrm.biz.activiti.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.activiti.engine.repository.Model;

import java.util.Map;

/**
 * @author entfrm
 * @date 2020/3/23
 */
public interface ModelService {

    /**
     * 流程分页查询
     *
     * @param params
     * @return
     */
    IPage<Model> list(Map<String, Object> params);

    /**
     * 创建流程
     *
     * @param name
     * @param key
     * @param desc
     * @param category
     * @return
     */
    Model save(String name, String key, String desc, String category);

    /**
     * 删除流程
     *
     * @param id
     * @return
     */
    Boolean removeById(String id);

    /**
     * 部署流程
     *
     * @param id
     * @return
     */
    Boolean deploy(String id);
}
