package com.example.activitifive.service;


import com.example.activitifive.model.Buyer;
import com.example.activitifive.model.Leave;
import com.example.activitifive.model.Seller;
import com.example.activitifive.model.Shopping;
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
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShoppingService {

    @Resource
     private ProcessEngine processEngine;

    @Resource
    private TaskService taskService;

    @Resource
    private RuntimeService runtimeService;

    /**
     * 1、部署流程
     *
     *    @1、将流程的文件以二进制的形式保存起来录入数据到： act_ge_bytearray 用于通用的流程定义和流程资源
     *    @2、生成部署数据： ACT_RE_DEPLOYMENT
     *    @3、生成流程定义数据： ACT_RE_PROCDEF
     */
    public Map<String, Object> deployProcessDefinition(){

        //获取部署资源
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment()
                .addClasspathResource("processes/shopping.bpmn")
                .deploy();

        Map<String,Object>  result = new HashMap<>();
        result.put("部署id：",deployment.getId());
        result.put("部署名称：",deployment.getName());
        result.put("部署时间：",deployment.getDeploymentTime());//部署时间
        return  result;
    }

    /**
     * 2、查看部署的流程信息
     *
     */
    public Map<String, Object> queryProcdef() {

        ProcessDefinitionQuery query = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("shopping");
        //执行查询获取流程定义明细
        List<ProcessDefinition> pds = query.list();

        List<Map<String,Object>> temp = new ArrayList<>();
        for (ProcessDefinition p : pds){
            Map<String,Object> item = new HashMap<>();
            item.put("流程id：",p.getId());
            item.put("流程名称：",p.getName());
            item.put("流程version：",p.getVersion());
            temp.add(item);
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("datas",temp);
        return  resultMap;
    }

    /**
     * 3、启动流程
     *
     *  @1、给指定的用户创建流程任务： act_ru_task
     *  @2、生成运行时流程执行实例： act_ru_execution 并生成对应的流程实列记录：ACT_HI_PROCINST
     *  @3、生成运行时用户关系实例： act_hi_identitylink
     *
     */
    public Map<String, Object> start(int buyerId,int sellerId) {

        Map<String,Object> vars = new HashMap<>();

        Buyer buyer = new Buyer();
        buyer.setUserID(buyerId);
        buyer.setPay(true);
        vars.put("buyer",buyer);

        Seller seller = new Seller();
        seller.setUserID(sellerId);
        seller.setDelivery(true);
        vars.put("seller",seller);

        Shopping shopping = new Shopping();
        shopping.setBuyerId(buyerId+"");
        shopping.setSellerId(sellerId+"");
        vars.put("shopping",shopping);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("shopping",vars);

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("流程id：",processInstance.getId());
        resultMap.put("流程名称：",processInstance.getName());
        resultMap.put("流程定义id：",processInstance.getProcessDefinitionId());

        return  resultMap;
    }

    /**
     * 4、查询买家 和 卖家的 任务列表
     *
     * @param userId
     * @return
     */
    public Map<String, Object> find(@RequestParam("userId") int userId) {

        TaskQuery query =taskService.createTaskQuery().processDefinitionKey("shopping");
        List<Task> tasks =  query.taskAssignee(""+userId).list();

        List<Shopping> shoppings = new ArrayList<>();
        for (Task t : tasks){
            System.out.println();
            Shopping shopping = (Shopping) taskService.getVariable(t.getId(),"shopping");
            shopping.setTaskId(t.getId());
            shopping.setTaskName(t.getName());
            shopping.setAssignee(t.getAssignee());
            shopping.setProcessInstanceId(t.getProcessInstanceId());
            shoppings.add(shopping);
        }

        Map<String,Object> result = new HashMap<>();
        result.put("datas",shoppings);
        return  result;
    }

    /**
     * 5、买家完成个人任务（付款、收货）
     *    卖家完成个人任务（发货、收款）
     *
     *  @1、根据用户任务创建历史流程记录： ACT_HI_ACTINST
     *  @2、买家完成付费任务，到卖家收款
     *  @3、卖家完成发货任务，到买家收货
     *
     */
    public Map<String, Object> doTask(Shopping shopping) {
        Task task = taskService.createTaskQuery().processDefinitionKey("shopping").taskId(shopping.getTaskId()).singleResult();
        //获取之前的变量值
        Shopping shop = null;
        if (task !=null){
            //获取之前的变量值
            shop = (Shopping)taskService.getVariable(task.getId(),"shopping");
            shop.setPay(shopping.isPay());
            shop.setDeliver(shopping.isDeliver());
            Map<String,Object> vars = new HashMap<>();
            vars.put("shopping",shop);
            taskService.complete(task.getId(),vars);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("datas",shop);
        return result;

    }

    /**
     * 6、查询历史流程实例 act_hi_procinst
     *
     *    查询历史流程实例，就是查找按照某个流程定义的规则一共执行了多少次流程，对应的数据库表：act_hi_procinst
     */
    public Map<String, Object> findHistoricProcessInstance() {
        HistoryService historyService = processEngine.getHistoryService();

        //List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("leave").variableValueEquals("leave.userId", userId).list();
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("shopping").list();
        //获取特定的流程定义历史数据
        //List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionId("testVariables:2:1704").list();

        List<  Map<String, Object>> temp = new ArrayList<>();
        if(list != null && list.size()>0){
            for(HistoricProcessInstance hi:list){
                Map<String, Object> item =  new HashMap<>();
                item.put("历史流程id：",hi.getId());
                item.put("历史开始时间：",hi.getStartTime());
                item.put("历史结束时间：",hi.getEndTime());
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
                item.put("活动id：",hai.getId());
                item.put("活动名称：",hai.getActivityName());
                item.put("活动开始时间：",hai.getStartTime());
                temp.add(item);
                System.out.println(hai.getId()+"  "+hai.getActivityName());
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
                item.put("任务id：",hti.getId());
                item.put("任务名称：",hti.getName());
                item.put("任务开始时间：",hti.getStartTime());
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
