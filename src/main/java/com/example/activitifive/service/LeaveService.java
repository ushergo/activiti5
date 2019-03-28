package com.example.activitifive.service;

import com.example.activitifive.controller.LeaveController;
import com.example.activitifive.model.Leave;
import com.example.activitifive.model.ProcessDefinitionModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
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
     *    @1、将流程的文件以二进制的形式保存起来录入数据到： act_ge_bytearray 用于通用的流程定义和流程资源
     *    @2、生成部署数据： ACT_RE_DEPLOYMENT
     *    @3、生成流程定义数据： ACT_RE_PROCDEF
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
     *  @1、给指定的用户创建流程任务： act_ru_task
     *  @2、生成运行时流程执行实例： act_ru_execution 并生成对应的流程实列记录：ACT_HI_PROCINST
     *  @3、生成运行时用户关系实例： act_hi_identitylink
     *
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
     * 5、完成个人任务（填写请假单）
     *
     *  @1、根据用户任务创建历史流程记录： ACT_HI_ACTINST
     *  @2、改变用户任务的Assign值，将任务交给下一位用户（初审主管）执行： ACT_RU_TASK
     *
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
     * 6、直接主管审批
     *
     *  @1、根据用户任务创建历史流程记录： ACT_HI_ACTINST
     *  @2、直接主管审核后，改变用户任务的Assign值，将任务交给下一位用户（复审主管）执行： ACT_RU_TASK
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
     * 7、部门主管审批
     *
     *  @1、复审主管审核后，清空该任务对应的所有运行时的数据： ACT_RU_TASK、ACT_RU_VARIABLE、ACT_RU_EXECUTION、ACT_RU_IDENTITYLINK
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
     * 8、查询历史流程实例 act_hi_procinst
     *
     *    查询历史流程实例，就是查找按照某个流程定义的规则一共执行了多少次流程，对应的数据库表：act_hi_procinst
     */
    public Map<String, Object> findHistoricProcessInstance() {
        HistoryService historyService = processEngine.getHistoryService();

        //List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("leave").variableValueEquals("leave.userId", userId).list();
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("leave").list();
        //获取特定的流程定义历史数据
        //List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionId("testVariables:2:1704").list();

        Map<String, Object> temp =  new HashMap<>();
        if(list != null && list.size()>0){
            int index = 0;
            for(HistoricProcessInstance hi:list){
                Map<String, Object> item =  new HashMap<>();
                item.put("历史流程id：",hi.getId());
                item.put("历史开始时间：",hi.getStartTime());
                item.put("历史结束时间：",hi.getEndTime());
                temp.put("item"+index,item);
                index++;
            }
        }

        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", temp);
        return resultMap;
    }

    /**/

    /**9、查询历史活动 act_hi_actinst
     *       查询历史活动，就是查询某一次流程的执行一共经历了多少个活动，这里我们使用流程定义ID来查询，对应的数据库表为:act_hi_actinst
     *       问题：HistoricActivityInstance对应哪个表
     *       问题：HistoricActivityInstance和HistoricTaskInstance有什么区别
     * @param processInstanceId 历史流程实列id，获取该流程下的用户所有操作活动
     * @return
     */
    public Map<String, Object>  findHisActivitiList(String processInstanceId){
        List<HistoricActivityInstance> list = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();

        Map<String, Object> temp =  new HashMap<>();
        if(list != null && list.size()>0){
            int index = 0;
            for(HistoricActivityInstance hai : list){
                Map<String, Object> item =  new HashMap<>();
                item.put("活动id：",hai.getId());
                item.put("活动名称：",hai.getActivityName());
                item.put("活动开始时间：",hai.getStartTime());
                temp.put("item"+index,item);
                System.out.println(hai.getId()+"  "+hai.getActivityName());
                index++;
            }
        }
        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", temp);
        return resultMap;
    }

    /**10、查询历史任务 act_hi_taskinst
     *     查询历史任务，就是查询摸一次流程的执行一共经历了多少个任务，对应表：act_hi_taskinst
     *
     * @param processInstanceId 历史流程实列id，获取该流程下的所有用户的历史任务
     * @return
     */
    public Map<String, Object>  findHisTaskList(String processInstanceId){
        List<HistoricTaskInstance> list = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        Map<String, Object> temp =  new HashMap<>();

        if(list!=null && list.size()>0){
            int index = 0;
            for(HistoricTaskInstance hti:list){
                Map<String, Object> item =  new HashMap<>();
                item.put("任务id：",hti.getId());
                item.put("任务名称：",hti.getName());
                item.put("任务开始时间：",hti.getStartTime());
                temp.put("item"+index,item);
                index++;
            }
        }
        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", temp);
        return resultMap;
    }

    /**11、查询历史流程变量 act_hi_varinst
     *     查询历史流程变量，就是查询某一次流程的执行一共设置的流程变量，对应表：act_hi_varinst
     *
     * @param processInstanceId 历史流程实列id，获取该流程下的所有用户的历史任务
     * @return
     */
    public Map<String, Object>  findHisVariablesList(String processInstanceId){

        List<HistoricVariableInstance> list = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();

        Map<String, Object> temp =  new HashMap<>();
        List<Map<String, Object>> itemlist = new ArrayList<>();

        if(list != null && list.size()>0){
            for(HistoricVariableInstance hvi:list){
                Map<String, Object> item =  new HashMap<>();
                System.out.println(hvi.getId()+"    "+hvi.getVariableName()+"	"+hvi.getValue());
                item.put("变量id：",hvi.getId());
                item.put("变量名称：",hvi.getVariableName());
                item.put("变量值：",hvi.getValue());
                itemlist.add(item);
            }
        }

        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", itemlist);
        return resultMap;
    }


}
