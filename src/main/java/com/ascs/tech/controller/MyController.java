/*
package com.ascs.tech.controller;

import com.ascs.tech.service.SoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class MyController {

    public static String tgt = "";

    @Value("${hik.username}")
    public String username;

    @Value("${hik.password}")
    public String password;

    @Value("${hik.serverIp}")
    public String serverIp;

    @Autowired
    SoapService soapService;

    @RequestMapping("/")
    public String home(){
        return "websocket";
    }

    @ResponseBody
    @RequestMapping("/token")
    public String getToken(){
        System.out.println(username + " : " + password + " : " + serverIp);
        String token = soapService.applyToken(tgt);
        if(token == null || token.equalsIgnoreCase("null")){
            tgt =soapService.sdkLogin(username, password, serverIp);
            token = soapService.applyToken(tgt);
        }
        return token;
    }

    @ResponseBody
    @RequestMapping("/getPreviewXml")
    public String getPreviewOcxOptions(){
//        String xml = soapService.getResourceByCodes();
        return null;
    }

}
*/
