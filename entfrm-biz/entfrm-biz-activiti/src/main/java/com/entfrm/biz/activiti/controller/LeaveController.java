package com.entfrm.biz.activiti.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.entfrm.biz.activiti.entity.Leave;
import com.entfrm.biz.activiti.service.LeaveService;
import com.entfrm.biz.activiti.service.ProcessService;
import com.entfrm.core.base.api.R;
import com.entfrm.core.base.enums.TaskStatusEnum;
import com.entfrm.core.base.util.ExcelUtil;
import com.entfrm.core.log.annotation.OperLog;
import com.entfrm.core.security.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 请假 controller
 */
@RestController
@AllArgsConstructor
@RequestMapping("/activiti/leave")
public class LeaveController {
    private final LeaveService leaveService;
    private final ProcessService processService;

    private QueryWrapper<Leave> getQueryWrapper(Leave leave) {
        return new QueryWrapper<Leave>().like(StrUtil.isNotBlank(leave.getUserName()), "user_name", leave.getUserName()).eq(StrUtil.isNotBlank(leave.getType()), "type", leave.getType()).eq(StrUtil.isNotBlank(leave.getStatus()), "status", leave.getStatus())
                .between(StrUtil.isNotBlank(leave.getBeginTime()) && StrUtil.isNotBlank(leave.getEndTime()), "create_time", leave.getBeginTime(), leave.getEndTime()).apply(StrUtil.isNotBlank(leave.getSqlFilter()), leave.getSqlFilter());
    }

    @GetMapping("/list")
    public R list(Page page, Leave leave) {
        IPage<Leave> leaveIPage = leaveService.page(page, getQueryWrapper(leave));
        return R.ok(leaveIPage.getRecords(), leaveIPage.getTotal());
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Integer id) {
        return R.ok(leaveService.getById(id));
    }

    @OperLog("请假新增")
    @PreAuthorize("@ps.hasPerm('leave_add')")
    @PostMapping("/save")
    public R save(@RequestBody Leave leave) {
        leave.setUserName(SecurityUtil.getUser().getUsername());
        leave.setStatus(TaskStatusEnum.UNSUBMIT.getStatus());
        return R.ok(leaveService.save(leave));
    }

    @OperLog("请假修改 ")
    @PreAuthorize("@ps.hasPerm('leave_edit')")
    @PutMapping("/update")
    public R update(@RequestBody Leave leave) {
        return R.ok(leaveService.updateById(leave));
    }

    /**
     * 删除
     *
     * @param id
     * @return R
     */
    @OperLog("请假删除")
    @PreAuthorize("@ps.hasPerm('leave_del')")
    @DeleteMapping("/{id}")
    public R removeById(@PathVariable Integer id) {
        return R.ok(leaveService.removeById(id));
    }

    /**
     * 启动请假流程
     *
     * @param id
     * @return R
     */
    @OperLog("启动请假流程")
    @PreAuthorize("@ps.hasPerm('leave_edit')")
    @GetMapping("/startProcess/{id}")
    public R startProcess(@PathVariable("id") Integer id) {
        return R.ok(processService.startLeaveProcess(id));
    }

    @OperLog("请假导出")
    @PreAuthorize("@ps.hasPerm('leave_export')")
    @GetMapping("/export")
    @ResponseBody
    public R export(Leave leave) {
        List<Leave> list = leaveService.list(getQueryWrapper(leave));
        ExcelUtil<Leave> util = new ExcelUtil<Leave>(Leave.class);
        return util.exportExcel(list, "请假数据");
    }
}
