package com.example.activitifive.controller;


import com.example.activitifive.service.LeaveService;
import com.example.activitifive.service.ShoppingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
