package com.example.activitifive.service;

import com.example.activitifive.common.ProcessHelper;
import com.example.activitifive.utils.ReflectionExecutor;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReceiveTaskService {


    ProcessHelper processHelper =new ProcessHelper("receiveTask","receiveTask");

    @Resource
    private ProcessEngine processEngine;

    public ReceiveTaskService() {
//        //第一步:加载xml文件
//        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
//        this.processHelper = (ProcessHelper) context.getBean("TaskServiceProcessHelper");
    }

    //部署流程
    public Map<String, Object> deploy() {

        Deployment deployment = processHelper.deployProcessDefinition("processes/receiveTask.bpmn");
        Map<String, Object> reslut = new HashMap<>();
        reslut.put("deployment_id", deployment.getId());
        reslut.put("deployment_name", deployment.getName());
        reslut.put("deployment_time", deployment.getDeploymentTime());
        return reslut;
    }

    //启动流程
    public Map<String, Object> start(Map<String, Object> vars) {
        Map<String, Object> result = new HashMap<>();
        ProcessInstance processInstance = processHelper.startProcess(vars);
        result.put("process_instance_id", processInstance.getId());
        result.put("process_instance_name", processInstance.getName());
        result.put("process_definition_id", processInstance.getProcessDefinitionId());
        result.put("process_definition_key", processInstance.getProcessDefinitionKey());
        result.put("process_instance_name", processInstance.getName());
        result.put("Process_definition_name", processInstance.getProcessDefinitionName());
        return result;
    }


    /**
     * 2、查看部署的流程信息
     */
    public Map<String, Object> queryProcdef() {
        //执行查询获取流程定义明细
        List<ProcessDefinition> pds = processHelper.queryProcdef();
        List<Map<String, Object>> temp = new ArrayList<>();
        for (ProcessDefinition p : pds) {
            Map<String, Object> item = new HashMap<>();
            item.put("流程id：", p.getId());
            item.put("流程名称：", p.getName());
            item.put("流程version：", p.getVersion());
            temp.add(item);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("datas", temp);
        return resultMap;
    }

    //collectSellsAmount
    //sendMessage
    //完成等待任务
    public Map<String, Object> doTask(String processInstanceId, String activityId, Map<String, Object> vars) {

        //设置中间异步执行事件
        List<ReflectionExecutor> list = new ArrayList<>();
        ReflectionExecutor reflectionExecutor = new ReflectionExecutor("com.example.activitifive.utils.reflection.ReceiveTaskHelper", "sendMessage", vars);
        list.add(reflectionExecutor);

        boolean result = processHelper.doReceiveTask(processInstanceId, activityId, vars, list);
        Map<String, Object> resultMap = new HashMap<>();

        //9.判断流程是否结束
        ProcessInstance nowPi = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (nowPi == null)
            resultMap.put("process_status", "流程结束");
        else
            resultMap.put("process_status", "流程运行中");
        if (result)
            resultMap.put("message", "ok");
        else
            resultMap.put("message", "no");
        return resultMap;
    }


}
