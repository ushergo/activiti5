package com.example.activitifive.service;

import com.example.activitifive.common.ProcessHelper;
import com.example.activitifive.utils.ReflectionExecutor;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiveTaskService {

    ProcessHelper processHelper = new ProcessHelper("receiveTask","receiveTask");

    //部署流程
    public Map<String ,Object> deploy(){

        Deployment deployment = processHelper.deployProcessDefinition("processes/receiveTask");
        Map<String,Object> reslut = new HashMap<>();
        reslut.put("deployment_id",deployment.getId());
        reslut.put("deployment_name",deployment.getName());
        reslut.put("deployment_time",deployment.getDeploymentTime());
        return  reslut;
    }

    //启动流程
    public  Map<String,Object> start(Map<String,Object> vars){
        Map<String,Object> result = new HashMap<>();
        ProcessInstance processInstance =  processHelper.startProcess(vars);
        result.put("process_instance_id",processInstance.getId());
        result.put("process_instance_name",processInstance.getName());
        result.put("process_definition_id",processInstance.getProcessDefinitionId());
        result.put("process_definition_key",processInstance.getProcessDefinitionKey());
        result.put("process_instance_name",processInstance.getName());
        result.put("Process_definition_name",processInstance.getProcessDefinitionName());
        return  result;
    }

    //完成等待任务
    public Map<String,Object> doTask(String processInstanceId,String activityId,Map<String,Object> vars){

        //设置中间异步执行事件
        List<ReflectionExecutor> list = new ArrayList<>();
        ReflectionExecutor reflectionExecutor = new ReflectionExecutor("com.example.activitifive.utils.reflection.ReceiveTaskHelper","sendMessage",vars);
        list.add(reflectionExecutor);

        boolean result =  processHelper.doReceiveTask(processInstanceId,activityId,vars,list);
        Map<String,Object> resultMap = new HashMap<>();
        if(result)
            resultMap.put("message","ok");
        else
            resultMap.put("message","no");
        return resultMap;
    }


}
