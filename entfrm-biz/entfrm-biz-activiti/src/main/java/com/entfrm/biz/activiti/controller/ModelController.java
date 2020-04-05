package com.entfrm.biz.activiti.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.entfrm.biz.activiti.dto.ModelDto;
import com.entfrm.biz.activiti.service.ModelService;
import com.entfrm.core.base.api.R;
import com.entfrm.core.log.annotation.OperLog;
import lombok.AllArgsConstructor;
import org.activiti.engine.repository.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 模型 controller
 */
@RestController
@RequestMapping("/activiti/model")
@AllArgsConstructor
public class ModelController {
    private final ModelService modelService;

    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        IPage<Model> modelPage = modelService.list(params);
        return R.ok(modelPage.getRecords(), modelPage.getTotal());
    }

    @OperLog("模型新增")
    @PostMapping("/save")
    public R save(@RequestBody @Valid ModelDto form) {
        modelService.save(form.getName(), form.getCategory(), form.getKey(), form.getDescription());
        return R.ok();
    }

    @OperLog("模型删除")
    @DeleteMapping("/{id}")
    public R remove(@PathVariable("id") String id) {
        return R.ok(modelService.removeById(id));

    }

    @OperLog("模型部署")
    @PostMapping("/deploy/{id}")
    public R deploy(@PathVariable("id") String id) {
        return R.ok(modelService.deploy(id));
    }
}
