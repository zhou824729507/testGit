package com.ascs.tech.controller;

import com.ascs.tech.service.SoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/websocket")
@Controller
public class VideoPreview {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    private static String tgt = "";

    @Value("${hik.username}")
    private String username;

    @Value("${hik.password}")
    private String password;

    @Value("${hik.serverIp}")
    private String serverIp;

    @Autowired
    SoapService soapService;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<VideoPreview> webSocketSet = new CopyOnWriteArraySet<VideoPreview>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
//        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
//        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);

        //群发消息
        for (VideoPreview item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发生错误时调用
     * */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    @Scheduled(fixedRate = 30000)
    public void sedMessage() throws IOException {
        String token = getToken();
//        System.out.println(" 群发消息 -------------------------" + webSocketSet.iterator().next());

        //群发消息
        for (VideoPreview item : webSocketSet) {
            try {
                item.sendMessage(token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }

    @RequestMapping("/")
    public String home(){
        return "videoPreview";
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
    @RequestMapping("/getResourceByCodes")
    public String getResourceByCodes(){
        String token = getToken();
        String resourceCode = soapService.getResourceByCodes(token, "001198");
        return resourceCode;
    }

    @ResponseBody
    @RequestMapping("/getPreviewXml")
    public String getPreviewOcxOptions(@RequestParam(value = "indexCode", required = true) String indexCode){
        String token = getToken();
        String xml = soapService.getPreviewOcxOptions(token, indexCode, serverIp);
        return xml;
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        VideoPreview.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        VideoPreview.onlineCount--;
    }
}