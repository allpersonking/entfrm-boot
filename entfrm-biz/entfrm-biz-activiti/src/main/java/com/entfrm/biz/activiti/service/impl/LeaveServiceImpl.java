package com.entfrm.biz.activiti.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.entfrm.biz.activiti.entity.Leave;
import com.entfrm.biz.activiti.mapper.LeaveMapper;
import com.entfrm.biz.activiti.service.LeaveService;
import org.springframework.stereotype.Service;

/**
 * @author entfrm
 * @date 2020/3/23
 * @description 请假 ServiceImpl
 */
@Service
public class LeaveServiceImpl extends ServiceImpl<LeaveMapper, Leave> implements LeaveService {

}
