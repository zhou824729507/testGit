package com.ascs.tech.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by garfield on 2016/10/12.
 */
public class AirQuality {
    static FileChannel foChannel;

    public static void main(String[] args) throws Exception {
        int month = 10;
        while (month <= 10) {
            int day = 30;
            int dayNum;
            if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                dayNum = 31;
            } else if (month == 2) {
                dayNum = 28;
            } else {
                dayNum = 30;
            }
            while (day <= dayNum) {
                int hour = 10;
                while (hour <= 14) {
                    String time = "2018-" + month + "-" + day + " " + hour + ":00";
                    getData(time);
                    hour++;
                }
                day++;
            }
            month++;
        }
        foChannel.close();
        foChannel = null;
        System.out.println(" over done");
    }

    public static void getData(String params) {
        //拼接xml请求,带有请求头
        String soapRequestData = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <GetLongGangStation xmlns=\"http://tempuri.org/\">\n" +
                "      <timepoint>" + params + "</timepoint>\n" +
                "    </GetLongGangStation>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>";


        try {
            String method = "http://203.91.44.4:32005/WebService.asmx?op=GetLongGangStation";//比如http://192.177.222.222:8888/services/Service_Name/Function_Name 请求地址
            PostMethod postMethod = new PostMethod(method);
            byte[] b = soapRequestData.getBytes("utf-8");
            InputStream is = new ByteArrayInputStream(b, 0, b.length);
            RequestEntity re = new InputStreamRequestEntity(is, b.length, "text/xml; charset=utf-8");
            postMethod.setRequestEntity(re);

            HttpClient httpClient = new HttpClient();
            int statusCode = httpClient.executeMethod(postMethod);
            //200说明正常返回数据
            if (statusCode != 200) {
                //internet error
                System.out.println(statusCode);
            }
            soapRequestData = postMethod.getResponseBodyAsString();
            String data = soapRequestData.substring(soapRequestData.indexOf("[") + 1, soapRequestData.indexOf("]")) + ",";
//            saveFile(data, "/home/doct/Downloads/LongGang/原型/lg_JoinUp_data/src/com/ascs/temp.json");
            System.out.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(String str, String filename) {

        try {
            ByteBuffer src = ByteBuffer.wrap(str.getBytes());
            foChannel = new FileOutputStream(filename, true).getChannel();
            foChannel.write(src);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}