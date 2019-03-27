package com.example.activitifive.controller;


import com.example.activitifive.model.Leave;
import com.example.activitifive.service.LeaveService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/leave/v1")
public class LeaveController {


    @Resource
    private LeaveService leaveService;

    /**
     * 1、部署流程
     *
     */
    @RequestMapping(value = "/deploy", method = RequestMethod.GET)
    public Map<String, Object>  deployProcessDefinition(){

        Map<String, Object> resultMap = leaveService.deployProcessDefinition();
        return resultMap;
    }


    /**
     * 2、查看部署的流程信息
     *
     */
    @RequestMapping(value = "/queryProcdef", method = RequestMethod.GET)
    public Map<String, Object> queryProcdef() {

        Map<String, Object> resultMap = leaveService.queryProcdef();
        return resultMap;

    }



    /**
     * 3、启动流程
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public Map<String, Object> start(@RequestParam String userId) {

        Map<String, Object> resultMap = leaveService.start(userId);
        return resultMap;

    }




    /**
     * 4、查询用户任务（获取用户流程）
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public Map<String, Object> find(@RequestParam("userId") String userId) {

        Map<String, Object> resultMap = leaveService.find(userId);
        return resultMap;
    }



    /**
     * 5、完成个人任务（填写请假单）
     *
     * @param leave
     * @return
     */
    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public Map<String, Object> apply(@RequestBody Leave leave) {
        Map<String, Object> resultMap = leaveService.apply(leave);
        return resultMap;
    }



    /**
     * 6、直接主管审批
     *
     * @param leave
     * @return
     */
    @RequestMapping(value = "/approve1", method = RequestMethod.POST)
    public Map<String, Object> approve1(@RequestBody Leave leave) {
        Map<String, Object> resultMap = leaveService.approve1(leave);
        return resultMap;
    }

    /**
     * 7、部门主管审批
     *
     * @param leave
     * @return
     */
    @RequestMapping(value = "/approve2", method = RequestMethod.POST)
    public Map<String, Object> approve2(@RequestBody Leave leave) {
        Map<String, Object> resultMap = leaveService.approve2(leave);
        return resultMap;
    }

    /**
     * 8、查询历史流程实例
     *
     * @return
     */
    @RequestMapping(value = "/findHistoricProcessInstance", method = RequestMethod.GET)
    public Map<String, Object> findHistoricProcessInstance() {
        Map<String, Object> resultMap = leaveService.findHistoricProcessInstance();
        return resultMap;
    }


    /**
     * 9、查询历史活动
     *
     * @return
     */
    @RequestMapping(value = "/findHisActivitiList", method = RequestMethod.GET)
    public Map<String, Object> findHisActivitiList(String processInstanceId) {
        Map<String, Object> resultMap = leaveService.findHisActivitiList(processInstanceId);
        return resultMap;
    }



    /**
     * 9、查询历史任务
     *
     * @return
     */
    @RequestMapping(value = "/findHisTaskList", method = RequestMethod.GET)
    public Map<String, Object> findHisTaskList(String processInstanceId) {
        Map<String, Object> resultMap = leaveService.findHisTaskList(processInstanceId);
        return resultMap;
    }
}
