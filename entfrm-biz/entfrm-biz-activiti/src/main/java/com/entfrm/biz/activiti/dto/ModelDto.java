package com.entfrm.biz.activiti.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author yong
 * @date 2020/3/24
 * @description 模型 dto
 */
@Data
public class ModelDto {
    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "标识不能为空")
    private String key;

    @NotBlank(message = "描述不能为空")
    private String description;
}
