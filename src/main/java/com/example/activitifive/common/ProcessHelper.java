package com.example.activitifive.common;

import com.example.activitifive.model.Buyer;
import com.example.activitifive.model.Seller;
import com.example.activitifive.model.Shopping;
import com.example.activitifive.utils.ReflectionExecutor;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcessHelper {

    private  String processDefId;
    private  String processDefKey;

    public ProcessHelper(String processDefId,String processDefKey){
      this.processDefId = processDefId;
      this.processDefKey = processDefKey;
    }

    @Resource
    private ProcessEngine processEngine;
    @Resource
    private TaskService taskService;
    @Resource
    private RuntimeService runtimeService;

    @Autowired
    ReflectionExecuteHelper reflectionExecuteHelper; //执行中间操作辅助类

    /**
     * 1、部署流程
     * @param classpathResource 资源文件的路径字符串
     * @return
     *
     * @1、将流程的文件以二进制的形式保存起来录入数据到： act_ge_bytearray 用于通用的流程定义和流程资源
     * @2、生成部署数据： ACT_RE_DEPLOYMENT
     * @3、生成流程定义数据： ACT_RE_PROCDEF
     */
    public Deployment deployProcessDefinition(String classpathResource){

        //获取部署资源
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(classpathResource)
                .deploy();
        return  deployment;
    }

    /**
     * 2、查看部署的流程信息
     *
     */
    public   List<ProcessDefinition> queryProcdef() {
        ProcessDefinitionQuery query = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(this.processDefKey);
        //执行查询获取流程定义明细
        List<ProcessDefinition> pds = query.list();
        return  pds;
    }

    /**
     * 3、启动流程
     *
     *
     *
     */
    /**
     * 3、启动流程
     * @param vars 变量参数
     * @return
     *
     * @1、给指定的用户创建流程任务： act_ru_task
     * @2、生成运行时流程执行实例： act_ru_execution 并生成对应的流程实列记录：ACT_HI_PROCINST
     * @3、生成运行时用户关系实例： act_hi_identitylink
     */
    public ProcessInstance startProcess(Map<String,Object> vars) {
        return  this.startProcess( vars,this.processDefKey);
    }

    /**
     * 3、启动流程
     * @param vars 变量参数
     * @param processDefKey 指定的流程key
     * @return
     *
     * @1、给指定的用户创建流程任务： act_ru_task
     * @2、生成运行时流程执行实例： act_ru_execution 并生成对应的流程实列记录：ACT_HI_PROCINST
     * @3、生成运行时用户关系实例： act_hi_identitylink
     */
    public ProcessInstance startProcess(Map<String,Object> vars,String processDefKey) {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefKey,vars);
        return  processInstance;
    }

    /**
     * 4、查询用户任务列表
     *
     * @param userId
     * @return
     */
    public List<Task> findUserTasksById(String userId) {
        TaskQuery query =taskService.createTaskQuery().processDefinitionKey(this.processDefKey);
        List<Task> tasks =  query.taskAssignee(userId).list();
        return  tasks;
    }

    /**
     * 5、完成用户任务
     *
     * @1、根据用户任务创建历史流程记录： ACT_HI_ACTINST
     *
     */
    public void doTask(String taskId) {
        Task task = taskService.createTaskQuery().processDefinitionKey(this.processDefKey).taskId(taskId).singleResult();
        taskService.complete(task.getId());
    }

    /**
     * 5、完成用户任务
     *
     * @1、根据用户任务创建历史流程记录： ACT_HI_ACTINST
     *
     */
    public void doTask(String taskId,Map<String, Object> vars) {
        Task task = taskService.createTaskQuery().processDefinitionKey(this.processDefKey).taskId(taskId).singleResult();
        taskService.complete(task.getId(),vars);
    }

    /**
     * 5、完成等待任务,中间没有任务需要完成
     *
     * @等待任务，和我们之前用户任务不太一样的是，在等待任务执行的时候，act_ru_task表中是没有数据的，只有完成用户任务（UserTask），该表中才有数据。
     *
     */
    public boolean doReceiveTask(String processInstanceId,String activityId,Map<String, Object> vars) {

        boolean reslut = true;
        try{
            //查询执行对象表,使用流程实例ID和当前活动的名称（receivetask1）
            Execution execution = processEngine.getRuntimeService()
                    .createExecutionQuery()
                    .processInstanceId(processInstanceId)//流程实例ID
                    .activityId(activityId)//当前活动的名称
                    .singleResult();
            if(!vars.isEmpty()){
                //set 流程变量
                for (Map.Entry<String, Object> entry : vars.entrySet()) {
                    processEngine.getRuntimeService().setVariable(execution.getId(), entry.getKey() , entry.getValue());
                }
            }
            //向后执行一步
            processEngine.getRuntimeService()
                    .signal(execution.getId());

        }catch (Exception e){
            reslut =false;
        }finally {

        }

        return  reslut;
    }




    /**
     * 5、完成等待任务,中间有任务需要完成
     *    @等待任务，和我们之前用户任务不太一样的是，在等待任务执行的时候，act_ru_task表中是没有数据的，只有完成用户任务（UserTask），该表中才有数据。
     * @param processInstanceId 流程实列id
     * @param activityId 活动id
     * @param vars 流程变量
     * @param reflectionExecutors 中间需要完成的任务映射集合（包括对应的类，函数，参数）
     * @return
     */
    public boolean doReceiveTask(String processInstanceId,String activityId,Map<String, Object> vars, List<ReflectionExecutor> reflectionExecutors) {

        boolean reslut = true;
        try{
            //查询执行对象表,使用流程实例ID和当前活动的名称（receivetask1）
            Execution execution = processEngine.getRuntimeService()
                    .createExecutionQuery()
                    .processInstanceId(processInstanceId)//流程实例ID
                    .activityId(activityId)//当前活动的名称
                    .singleResult();
            if(!vars.isEmpty()){
                //set 流程变量
                for (Map.Entry<String, Object> entry : vars.entrySet()) {
                    processEngine.getRuntimeService().setVariable(execution.getId(), entry.getKey() , entry.getValue());
                }
            }

            //执行中间动作
            reflectionExecuteHelper.executeSomething(reflectionExecutors);

            //向后执行一步
            processEngine.getRuntimeService()
                    .signal(execution.getId());

        }catch (Exception e){
            reslut =false;
        }finally {

        }

        return  reslut;
    }

    /**
     * 6、查询历史流程实例 act_hi_procinst
     *
     *    查询历史流程实例，就是查找按照某个流程定义的规则一共执行了多少次流程，对应的数据库表：act_hi_procinst
     */
    public Map<String, Object> findHistoricProcessInstance() {
        HistoryService historyService = processEngine.getHistoryService();

        //List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("leave").variableValueEquals("leave.userId", userId).list();
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(this.processDefKey).list();
        //获取特定的流程定义历史数据
        //List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionId("testVariables:2:1704").list();

        List<Map<String, Object>> temp = new ArrayList<>();
        if(list != null && list.size()>0){
            for(HistoricProcessInstance hi:list){
                Map<String, Object> item =  new HashMap<>();
                item.put("his_process_id",hi.getId());
                item.put("his_process_start_time",hi.getStartTime());
                item.put("his_process_end_time",hi.getEndTime());
                temp.add(item);
            }
        }

        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", temp);
        return resultMap;
    }

    /**/

    /**7、查询历史活动 act_hi_actinst
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

        List<  Map<String, Object>> temp = new ArrayList<>();
        if(list != null && list.size()>0){
            for(HistoricActivityInstance hai : list){
                Map<String, Object> item =  new HashMap<>();
                item.put("his_act_id",hai.getId());
                item.put("his_act_name",hai.getActivityName());
                item.put("his_act_start_time",hai.getStartTime());
                item.put("his_act_end_time",hai.getEndTime());
                item.put("his_act_excution_id",hai.getExecutionId());
                item.put("his_act_assignee",hai.getAssignee());
                item.put("his_act_task_id",hai.getTaskId());
                item.put("his_act_process_definition_id",hai.getProcessDefinitionId());
                temp.add(item);
            }
        }
        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", temp);
        return resultMap;
    }

    /**8、查询历史任务 act_hi_taskinst
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
        List<  Map<String, Object>> temp = new ArrayList<>();
        if(list!=null && list.size()>0){
            for(HistoricTaskInstance hti:list){
                Map<String, Object> item =  new HashMap<>();
                item.put("his_task_id",hti.getId());
                item.put("his_task_name",hti.getName());
                item.put("his_task_start_time",hti.getStartTime());
                item.put("his_task_end_time",hti.getEndTime());
                temp.add(item);
            }
        }
        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", temp);
        return resultMap;
    }

    /**9、查询历史流程变量 act_hi_varinst
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

        List<Map<String, Object>> itemlist = new ArrayList<>();
        if(list != null && list.size()>0){
            for(HistoricVariableInstance hvi:list){
                Map<String, Object> item =  new HashMap<>();
                System.out.println(hvi.getId()+"    "+hvi.getVariableName()+"	"+hvi.getValue());
                item.put("his_var_id",hvi.getId());
                item.put("his_var_name",hvi.getVariableName());
                item.put("his_var_value",hvi.getValue());
                itemlist.add(item);
            }
        }

        Map<String, Object> resultMap =  new HashMap<>();
        resultMap.put("datas", itemlist);
        return resultMap;
    }

}
