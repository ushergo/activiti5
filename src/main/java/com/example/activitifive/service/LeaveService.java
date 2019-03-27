package com.example.activitifive.service;

import com.example.activitifive.controller.LeaveController;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaveService {



    public static final Logger log = LoggerFactory.getLogger(LeaveController.class);

    /*说明
          流程：指完成某件事情需要的过程.
          流程定义：具体的流程.把具体流程定义在bpmn文件中.
          流程部署：把流程定义文件保存到数据库.
    */

    //运行时服务
    @Resource
    private RuntimeService runtimeService;

    //任务服务
    @Resource
    private TaskService taskService;

    //流程引擎
    @Resource
    private ProcessEngine processEngine;

    /**
     * 1、部署流程
     *
     */
    public Map<String, Object> deployProcessDefinition(){

        Deployment deployment = processEngine.getRepositoryService()//获取流程定义和部署相关的Service
                .createDeployment()//创建部署对象
                .addClasspathResource("processes/leave.bpmn")
                .addClasspathResource("processes/leave.png")
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
            processDefinitionModels.add(p);

            System.out.println("ID:"+pd.getId()
                    +",NAME:"+pd.getName()
                    +",KEY:"+pd.getKey()
                    +",VERSION:"+pd.getVersion()
                    +",RESOURCE_NAME:"+pd.getResourceName()
                    +",DGRM_RESOURCE_NAME:"+pd.getDiagramResourceName());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("datas",processDefinitionModels);
        return resultMap;


    }



    /**
     * 3、启动流程
     *
     * @param userId
     * @return
     */
    public Map<String, Object> start(@RequestParam String userId) {
        Map<String, Object> vars = new HashMap<>();
        Leave leave = new Leave();
        leave.setUserId(userId);
        vars.put("leave", leave);
        //正在执行的操作 ,启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave", vars); //注意leave为bpmn图的id

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("流程实例ID：",processInstance.getId());//流程实例ID：101
        resultMap.put("流程实例ProcessInstanceId：",processInstance.getProcessInstanceId());//流程实例ID：101
        resultMap.put("流程实例ProcessDefinitionId：",processInstance.getProcessDefinitionId());//leave1:1:4

        return resultMap;
    }




    /**
     * 4、查询用户任务（获取用户流程）
     *
     * @param userId
     * @return
     */
    public Map<String, Object> find(@RequestParam("userId") String userId) {

        //获取任务服务对象
        TaskService taskService = processEngine.getTaskService();
        //根据接受人获取该用户的任务 //通过当前任务办理人 userId 获取任务列表
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId) //获取的就是设置用户图标的assignee值！！
                .list();

        for (Task task : tasks) {
            System.out.println("ID:"+task.getId()
                    +",姓名:"+task.getName()
                    +",接收人:"+task.getAssignee()
                    +",开始时间:"+task.getCreateTime());
        }
        List<Leave> resultList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tasks)) {
            for (Task task : tasks) {
                //获取对象
                Leave leave = (Leave) taskService.getVariable(task.getId(), "leave");
                leave.setTaskId(task.getId());
                leave.setTaskName(task.getName());
                leave.setApprover1(task.getAssignee());
                leave.setStartDate(task.getCreateTime());

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
    public Map<String, Object> apply(@RequestBody Leave leave) {
        Task task = taskService.createTaskQuery().taskId(leave.getTaskId()).singleResult();
        Map<String, Object> vars = new HashMap<>();
        Leave origin = (Leave) taskService.getVariable(leave.getTaskId(), "leave");
        origin.setDesc(leave.getDesc());
        origin.setStartDate(leave.getStartDate());
        origin.setEndDate(leave.getEndDate());
        origin.setTotalDay(leave.getTotalDay());
        origin.setApprover1(leave.getApprover1()); //todo 设置初审人员（直接主管）！
        origin.setApprover2(leave.getApprover2()); //todo 设置复审人员（部门主管）！
        origin.setSubmit(leave.getSubmit()); //设置是否提交 true表示提交，false表示不提交
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
    public Map<String, Object> approve1(@RequestBody Leave leave) {

        System.out.println("TaskId = "+leave.getTaskId());

        Task task = taskService.createTaskQuery().taskId(leave.getTaskId()).singleResult();
        Map<String, Object> vars = new HashMap<>();
        Leave origin = (Leave) taskService.getVariable(leave.getTaskId(), "leave");
        origin.setApproveDesc1(leave.getApproveDesc1());
        origin.setAgree1(leave.getAgree1());

        System.out.println("approver2 = "+origin.getApprover2());
        System.out.println("getApproveDesc1 =  "+origin.getApproveDesc1());

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
