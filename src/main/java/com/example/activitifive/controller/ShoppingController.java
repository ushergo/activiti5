package com.example.activitifive.controller;


import com.example.activitifive.model.Leave;
import com.example.activitifive.model.Shopping;
import com.example.activitifive.service.LeaveService;
import com.example.activitifive.service.ShoppingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/shopping/v1")
public class ShoppingController {

    @Resource
    private ShoppingService shoppingService;

    /**
     * 1、部署流程
     *
     */
    @RequestMapping(value = "/deploy", method = RequestMethod.GET)
    public Map<String, Object> deployProcessDefinition(){

        Map<String, Object> resultMap = shoppingService.deployProcessDefinition();
        return resultMap;
    }


    /**
     * 2、查看部署的流程信息
     *
     */
    @RequestMapping(value = "/queryProcdef", method = RequestMethod.GET)
    public Map<String, Object> queryProcdef() {

        Map<String, Object> resultMap = shoppingService.queryProcdef();
        return resultMap;

    }



    /**
     * 3、启动流程
     *
     */
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public Map<String, Object> start(@RequestParam int buyerId,@RequestParam  int sellerId) {

        Map<String, Object> resultMap = shoppingService.start(buyerId,sellerId);
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

        Map<String, Object> resultMap = shoppingService.find(userId);
        return resultMap;
    }

    /**
     * 5、买家完成个人任务（付款 收货）
     *
     * @param shopping
     * @return
     */
    @RequestMapping(value = "/buyOrArrive", method = RequestMethod.POST)
    public Map<String, Object> buyOrArrive(@RequestBody Shopping shopping) {
        Map<String, Object> resultMap = shoppingService.doTask(shopping);
        return resultMap;
    }

    /**
     * 6、卖家完成个人任务（发货、收款）
     *
     * @param shopping
     * @return
     */
    @RequestMapping(value = "/deliverOrReceive", method = RequestMethod.POST)
    public Map<String, Object> deliverOrRecieve(@RequestBody Shopping shopping) {
        Map<String, Object> resultMap = shoppingService.doTask(shopping);
        return resultMap;
    }

    /**
     * 7、查询历史流程实例
     *
     * @return
     */
    @RequestMapping(value = "/findHistoricProcessInstance", method = RequestMethod.GET)
    public Map<String, Object> findHistoricProcessInstance() {
        Map<String, Object> resultMap = shoppingService.findHistoricProcessInstance();
        return resultMap;
    }


    /**
     * 8、查询历史活动
     *
     * @return
     */
    @RequestMapping(value = "/findHisActivitiList", method = RequestMethod.GET)
    public Map<String, Object> findHisActivitiList(String processInstanceId) {
        Map<String, Object> resultMap = shoppingService.findHisActivitiList(processInstanceId);
        return resultMap;
    }



    /**
     * 9、查询历史任务
     *
     * @return
     */
    @RequestMapping(value = "/findHisTaskList", method = RequestMethod.GET)
    public Map<String, Object> findHisTaskList(String processInstanceId) {
        Map<String, Object> resultMap = shoppingService.findHisTaskList(processInstanceId);
        return resultMap;
    }

    /**
     * 10、查询历史流程变量
     *
     * @return
     */
    @RequestMapping(value = "/findHisVariablesList", method = RequestMethod.GET)
    public Map<String, Object> findHisVariablesList(String processInstanceId) {
        Map<String, Object> resultMap = shoppingService.findHisVariablesList(processInstanceId);
        return resultMap;
    }

}
