package com.example.activitifive.utils.reflection;

import java.util.Map;

public class ReceiveTaskHelper {

    /**
     * 给某人发消息
     * @param vars
     */
    public  void  sendMessage(Map<String,Object> vars){
        System.out.println("send message to "+vars.get("userName"));
    }
}
