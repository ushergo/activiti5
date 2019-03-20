package com.example.activitifive.controller;


import com.example.activitifive.model.Leave;
import com.example.activitifive.model.ProcessDefinitionModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave/v1")
public class LeaveController {

    public static final Logger log = LoggerFactory.getLogger(LeaveController.class);

    /*说明
          流程：指完成某件事情需要的过程.
          流程定义：具体的流程.把具体流程定义在bpmn文件中.
          流程部署：把流程定义文件保存到数据库.
    */


    //运行时服务
    @Autowired
    private RuntimeService runtimeService;

    //任务服务
    @Autowired
    private TaskService taskService;

    //流程引擎
    @Autowired
    private ProcessEngine processEngine;


    /**
     * 1、部署流程
     *
     */
    @RequestMapping(value = "/deploy", method = RequestMethod.GET)
    public Map<String, Object>  deployProcessDefinition(){

        Deployment deployment = processEngine.getRepositoryService()//获取流程定义和部署相关的Service
                .createDeployment()//创建部署对象
                .addClasspathResource("processes/leave.bpmn")
//                .addClasspathResource("processes/leave.png")
                .deploy();//完成部署

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("部署ID：",deployment.getId());//部署ID:1
        resultMap.put("部署时间：",deployment.getDeploymentTime());//部署时间
        return resultMap;
    }


    /**
     * 2、查看部署的流程信息
     *
     */
    @RequestMapping(value = "/queryProcdef", method = RequestMethod.GET)
    public Map<String, Object> queryProcdef() {

        RepositoryService repositoryService = processEngine.getRepositoryService();
        //创建查询对象
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        //添加查询条件
        query.processDefinitionKey("leave");//通过key获取
        // .processDefinitionName("My process")//通过name获取
        // .orderByProcessDefinitionId()//根据ID排序
        //执行查询获取流程定义明细
        List<ProcessDefinition> pds = query.list();
        log.debug("pds.length  = "+pds.size());

        List<ProcessDefinitionModel> processDefinitionModels = new ArrayList<>();
        for (ProcessDefinition pd : pds) {
            ProcessDefinitionModel p = new ProcessDefinitionModel();
            p.setId(pd.getId());
            p.setName(pd.getName());
            p.setCategory(pd.getCategory());
            p.setDeploymentId(pd.getDeploymentId());
            p.setDescription(pd.getDescription());
            p.setDiagramResourceName(pd.getDiagramResourceName());
            p.setKey(pd.getKey());
            p.setResourceNamed(pd.getDiagramResourceName());
            p.setSuspended(pd.isSuspended());
            processDefinitionModels.add(p);

            log.debug("ID:"+pd.getId()
                    +",NAME:"+pd.getName()
                    +",KEY:"+pd.getKey()
                    +",VERSION:"+pd.getVersion()
                    +",RESOURCE_NAME:"+pd.getResourceName()
                    +",DGRM_RESOURCE_NAME:"+pd.getDiagramResourceName());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("datas", pds);
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
        Map<String, Object> vars = new HashMap<>();
        Leave leave = new Leave();
        leave.setUserId(userId);
        vars.put("leave", leave);
        //正在执行的操作 ,启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave", vars); //注意leave1为bpmn图的id

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("流程实例ID：",processInstance.getId());//流程实例ID：101
        resultMap.put("流程实例ProcessInstanceId：",processInstance.getProcessInstanceId());//流程实例ID：101
        resultMap.put("流程实例ProcessDefinitionId：",processInstance.getProcessDefinitionId());//leave1:1:4

        return resultMap;
    }




    /**
     * 4、查看个人任务（获取用户流程）
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public Map<String, Object> find(@RequestParam("userId") String userId) {
        //通过当前任务办理人 userId 获取任务列表
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(userId).list();

        System.out.println(taskList.size());
        log.debug(taskList.size()+"");

        List<Leave> resultList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(taskList)) {
            for (Task task : taskList) {
                //获取对象
                Leave leave = (Leave) taskService.getVariable(task.getId(), "leave");
                leave.setTaskId(task.getId());
                leave.setTaskName(task.getName());
                resultList.add(leave);
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("datas", resultList);
        return resultMap;
    }



    /**
     * 完成个人任务（填写请假单）
     *
     * @param leave
     * @return
     */
    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public Map<String, Object> apply(@RequestBody Leave leave) {
        Task task = taskService.createTaskQuery().taskId(leave.getTaskId()).singleResult();
        Map<String, Object> vars = new HashMap<>();
        Leave origin = (Leave) taskService.getVariable(leave.getTaskId(), "leave");
        origin.setDesc(leave.getDesc());
        origin.setStartDate(leave.getStartDate());
        origin.setEndDate(leave.getEndDate());
        origin.setTotalDay(leave.getTotalDay());
        origin.setApprover1(leave.getApprover1());
        origin.setApprover2(leave.getApprover2());
        origin.setSubmit(leave.getSubmit());
        vars.put("leave", origin);
        taskService.complete(leave.getTaskId(), vars);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("datas",leave);
        return resultMap;
    }



    /**
     * 直接主管审批
     *
     * @param leave
     * @return
     */
    @RequestMapping(value = "/approve1", method = RequestMethod.POST)
    public Map<String, Object> approve1(@RequestBody Leave leave) {
        Task task = taskService.createTaskQuery().taskId(leave.getTaskId()).singleResult();
        Map<String, Object> vars = new HashMap<>();
        Leave origin = (Leave) taskService.getVariable(leave.getTaskId(), "leave");
        origin.setApproveDesc1(leave.getApproveDesc1());
        origin.setAgree1(leave.getAgree1());
        vars.put("leave", origin);
        taskService.complete(leave.getTaskId(), vars);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("datas",origin);
        return resultMap;
    }

    /**
     * 部门主管审批
     *
     * @param leave
     * @return
     */
    @RequestMapping(value = "/approve2", method = RequestMethod.POST)
    public Map<String, Object> approve2(@RequestBody Leave leave) {
        Task task = taskService.createTaskQuery().taskId(leave.getTaskId()).singleResult();
        Map<String, Object> vars = new HashMap<>();
        Leave origin = (Leave) taskService.getVariable(leave.getTaskId(), "leave");
        origin.setApproveDesc2(leave.getApproveDesc2());
        origin.setAgree2(leave.getAgree2());
        vars.put("leave", origin);
        taskService.complete(leave.getTaskId(), vars);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("datas",origin);
        return resultMap;
    }

    /**
     * 查看历史记录
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/findClosed", method = RequestMethod.GET)
    public Map<String, Object> findClosed(String userId) {
        HistoryService historyService = processEngine.getHistoryService();

        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("leave1").variableValueEquals("leave.userId", userId).list();
        List<Leave> leaves = new ArrayList<>();
        for (HistoricProcessInstance pi : list) {
            leaves.add((Leave) pi.getProcessVariables().get("leave"));
        }
        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", leaves);
        return resultMap;
    }


}
