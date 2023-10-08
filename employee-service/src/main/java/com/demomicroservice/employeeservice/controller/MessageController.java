package com.demomicroservice.employeeservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class MessageController {

    @Value("${spring.boot.message}")
    private String message;

    //http://localhost:8083/message
    /** After Change the Message in Git, Refresh the Bus - Method(Post)
     *
     *
     * All the service are refreshing when the bus refreshed*/
    //http://localhost:8083/actuator/busrefresh
    @GetMapping("/users/message")
    public String message(){
        return message;
    }
}
