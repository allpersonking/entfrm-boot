package com.entfrm.biz.activiti.controller;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.entfrm.biz.activiti.dto.LeaveDto;
import com.entfrm.biz.activiti.dto.ProcessDto;
import com.entfrm.biz.activiti.dto.TaskDto;
import com.entfrm.biz.activiti.service.TaskService;
import com.entfrm.core.base.api.R;
import com.entfrm.core.log.annotation.OperLog;
import com.entfrm.core.security.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Map;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 任务 controller
 */
@RestController
@AllArgsConstructor
@RequestMapping("/activiti/task")
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        IPage<TaskDto> taskIPage = taskService.list(params);
        return R.ok(taskIPage.getRecords(), taskIPage.getTotal());
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable String id) {
        return R.ok(taskService.getTaskById(id));
    }

    @OperLog("任务审批")
    @PostMapping("/checkTask")
    public R checkTask(@RequestBody LeaveDto leaveDto) {
        return R.ok(taskService.checkTask(leaveDto));
    }

    @OperLog("任务审批意见")
    @GetMapping("/commitList/{id}")
    public R commitList(@PathVariable String id) {
        return R.ok(taskService.commitList(id));
    }

    @OperLog("任务追踪")
    @GetMapping("/track/{id}")
    public ResponseEntity trackImage(@PathVariable String id) {
        InputStream imageStream = taskService.trackImage(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity(IoUtil.readBytes(imageStream), headers, HttpStatus.CREATED);
    }

}
