package com.entfrm.biz.activiti.listener;

import com.entfrm.core.security.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * @author entfrm
 * @date 2020/4/4
 * @description 请假流程监听器
 */
@Slf4j
public class LeaveTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        //查询当前上级 提醒上级审批并创建数据

        //此处实现给自己审批
        log.info("审批人：{}", SecurityUtil.getUser().getUsername());
        delegateTask.addCandidateUser(SecurityUtil.getUser().getUsername());

    }
}
