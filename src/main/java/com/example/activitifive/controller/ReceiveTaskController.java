package com.example.activitifive.controller;


import com.example.activitifive.model.Shopping;
import com.example.activitifive.service.ReceiveTaskService;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/receiveTask/v1")
public class ReceiveTaskController {

    @Autowired
    private ReceiveTaskService receiveTaskService;


    /**
     * 1、部署流程
     *
     */
    @RequestMapping(value = "/deploy", method = RequestMethod.GET)
    public Map<String, Object> deployProcessDefinition(){

        Map<String, Object> resultMap = receiveTaskService.deploy();
        return resultMap;
    }


    /**
     * 2、查看部署的流程信息
     *
     */
    @RequestMapping(value = "/queryProcdef", method = RequestMethod.GET)
    public Map<String, Object> queryProcdef() {

        Map<String, Object> resultMap = receiveTaskService.queryProcdef();
        return resultMap;
    }



    /**
     * 3、启动流程
     *
     */
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public Map<String, Object> start(@RequestParam int buyerId, @RequestParam  int sellerId) {

        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> resultMap = receiveTaskService.start(vars);
        return resultMap;
    }




    /**
     * 4、查询用户任务（获取用户流程）
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public Map<String, Object> find(@RequestParam("userId") int userId) {

        Map<String, Object> resultMap = receiveTaskService.queryProcdef();
        return resultMap;
    }

    /**
     * 5、完成用户任务
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/doTask", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> buyOrArrive(String processInstanceId, String activityId,@RequestParam("varis") String varis) {

        JSONObject jsonObject = JSONObject.fromObject(varis);
        Map<String,Object> vars = new HashMap<>();

        Iterator<String> i = jsonObject.keys();
        while (i.hasNext()){
            String key = i.next();
            vars.put(key,jsonObject.get(key));
        }
        Map<String, Object> resultMap = receiveTaskService.doTask(processInstanceId, activityId,vars);
        return resultMap;
    }


}
