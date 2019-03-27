package com.example.activitifive;

import com.example.activitifive.controller.LeaveController;
import com.example.activitifive.model.Leave;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitifiveApplicationTests {

    public static final Logger log = LoggerFactory.getLogger(LeaveController.class);

    @Autowired
    LeaveController l;

    @Test
    public void contextLoads() {

//        log.debug(l.find("3000").toString());
        System.out.println("datas:");
    }




}
