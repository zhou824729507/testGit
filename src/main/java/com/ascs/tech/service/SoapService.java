package com.ascs.tech.service;

import com.ascs.tech.utils.XmlUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Iterator;

/**
 * Created by garfield on 2016/10/12.
 */
@Service
@EnableAsync
public class SoapService {
    static FileChannel foChannel;

    private static final String url_path = "http://192.168.140.251/vms/services/VmsSdkWebService?wsdl";//比如http://192.177.222.222:8888/services/Service_Name/Function_Name 请求地址

    private static Log log = LogFactory.getLog(SoapService.class);

    public String sdkLogin(String username, String password, String serverIp) {

        String[] inputParamsName = {"username", "password", "serverIp"};
        String[] inputParamsValue = {username, password, serverIp};
        String[] outputParamsName = {"tgt"};
        String soapRequestData = getSoapRequestData("sdkLogin", inputParamsName, inputParamsValue);
        String tgt = getData(soapRequestData, outputParamsName);

        log.info("tgt : " + tgt);
        return tgt;

    }

    public String applyToken(String tgt) {

        String[] inputParamsName = {"tgt"};
        String[] inputParamsValue = {tgt};
        String[] outputParamsName = {"st"};

        String soapRequestData = getSoapRequestData("applyToken", inputParamsName, inputParamsValue);
        String token = getData(soapRequestData, outputParamsName);

        log.info("token : " + token);
        return token;
    }


    public String getResourceByCodes(String token, String resCodes) {

        String[] inputParamsName = {"token", "resType", "resCodes"};
        String[] inputParamsValue = {token, "10000", resCodes};
        String[] outputParamsName = {"c_index_code"};

        String soapRequestData = getSoapRequestData("getResourceByCodes", inputParamsName, inputParamsValue);
        String resource = getData(soapRequestData, outputParamsName);
        return resource;
    }

    public String getPreviewOcxOptions(String token, String cameraIndexCode, String clientIp) {

        String[] inputParamsName = {"token", "cameraIndexCode", "clientIp"};
        String[] inputParamsValue = {token, cameraIndexCode, clientIp};
        String[] outputParamsName = {};

        String soapRequestData = getSoapRequestData("getPreviewOcxOptions", inputParamsName, inputParamsValue);
        String xmlStr = getData(soapRequestData, outputParamsName);

        log.info("xml : " + xmlStr );
        return xmlStr;
    }

    public static String getData(String soapRequestData, String[] paramsName) {
        //拼接xml请求,带有请求头
        try {
            PostMethod postMethod = new PostMethod(url_path);
            byte[] b = soapRequestData.getBytes("utf-8");
            InputStream is = new ByteArrayInputStream(b, 0, b.length);
            RequestEntity re = new InputStreamRequestEntity(is, b.length, "text/xml; charset=utf-8");
            postMethod.setRequestEntity(re);

            HttpClient httpClient = new HttpClient();
            int statusCode = httpClient.executeMethod(postMethod);
            //200说明正常返回数据
            if (statusCode != 200) {
                //internet error
                log.info("statusCode : " + statusCode);
                log.error("请求失败！" + soapRequestData);
                return null;
            }

            soapRequestData = postMethod.getResponseBodyAsString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resolverXML(soapRequestData, paramsName);
    }

    public static String getSoapRequestData(String methodName, String[] inputParamsName, String[] inputParamsValue) {
        StringBuffer sb = new StringBuffer("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.vms.ivms6.hikvision.com\">");
        sb.append("<soapenv:Header/>");
        sb.append("<soapenv:Body>");
        sb.append("<ws:" + methodName + ">");

        for (int i = 0; i < inputParamsName.length; i++) {
            if (methodName.equalsIgnoreCase("getResourceByCodes") && inputParamsName[i].equalsIgnoreCase("resCodes")) {
                sb.append("<ws:resCodes xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>");
            }
            sb.append("<ws:" + inputParamsName[i] + ">" + inputParamsValue[i] + "</ws:" + inputParamsName[i] + ">");
        }

        sb.append("</ws:" + methodName + ">");
        sb.append("</soapenv:Body>");
        sb.append("</soapenv:Envelope>");

        return sb.toString();

    }

    public static String resolverXML(String xml, String[] outputParamsName) {

        Document doc = null;
        String resutlStr = null;

        try {
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML

            Element rootElt = doc.getRootElement(); // 获取根节点

            Iterator iter = rootElt.elementIterator("Body");
            while (iter.hasNext()) {

                Element recordEle = (Element) iter.next();

                Iterator iterator = recordEle.elements().iterator();
                while (iterator.hasNext()) {
                    Element recordEle1 = (Element) iterator.next();
                    String xmlResult = recordEle1.elementTextTrim("return");

                    if (outputParamsName.length == 0) {
                        return xmlResult;
                    }

                    Document doc_result = DocumentHelper.parseText(xmlResult);

                    Element rootElt_result = doc_result.getRootElement(); // 获取根节点

                    Iterator iterator_table = rootElt_result.element("rows").elementIterator();
                    while (iterator_table.hasNext()) {
                        Element row = (Element) iterator_table.next();
                        resutlStr = row.attributeValue(outputParamsName[0]);
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            log.error("找不到该节点");
        }

        return resutlStr;
    }

}