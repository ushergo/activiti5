package com.example.activitifive.service;


import com.example.activitifive.model.Buyer;
import com.example.activitifive.model.Seller;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.stereotype.Service;
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

        ProcessDefinitionQuery query = processEngine.getRepositoryService().createProcessDefinitionQuery();
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

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("shopping",vars);

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("流程id：",processInstance.getId());
        resultMap.put("流程名称：",processInstance.getName());
        resultMap.put("流程定义id：",processInstance.getProcessDefinitionId());

        return  resultMap;
    }

    /**
     * 4、查询买家任务
     *
     * @param userId
     * @return
     */
    public Map<String, Object> find(@RequestParam("userId") int userId) {

        TaskQuery query =taskService.createTaskQuery();
        List<Task> tasks =  query.taskAssignee(userId+"").list();

        List<Buyer> buyers = new ArrayList<>();
        for (Task t : tasks){
            Buyer buyer = (Buyer) taskService.getVariable(t.getId(),"buyer");
            buyers.add(buyer);
        }

        Map<String,Object> result = new HashMap<>();
        result.put("datas",buyers);
        return  result;
    }
}
