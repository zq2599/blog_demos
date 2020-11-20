package com.openfaas.function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openfaas.model.IRequest;
import com.openfaas.model.IResponse;
import com.openfaas.model.Response;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Handler extends com.openfaas.model.AbstractHandler {

    private static final String PARAM_USER_NAME = "name";


    private static final String RESPONSE_TEMPLETE = "Hello %s, response from [%s], PID [%s], %s";

    private ObjectMapper mapper = new ObjectMapper();


    /**
     * 获取本机IP地址
     * @return
     */
    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }

    /**
     * 返回当前进程ID
     * @return
     */
    private static String getPID() {
        return ManagementFactory
                .getRuntimeMXBean()
                .getName()
                .split("@")[0];
    }


    private String getUserName(IRequest req) {
        // 如果从请求body中取不到userName，就用
        String userName = null;

        try {
            Map<String, Object> mapFromStr = mapper.readValue(req.getBody(),
                    new TypeReference<Map<String, Object>>() {});

            if(null!=mapFromStr && mapFromStr.containsKey(PARAM_USER_NAME)) {
                userName = String.valueOf(mapFromStr.get(PARAM_USER_NAME));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果从请求body中取不到userName，就给个默认值
        if(StringUtils.isBlank(userName)) {
            userName = "anonymous";
        }

        return userName;
    }

    public IResponse Handle(IRequest req) {

        String userName = getUserName(req);

        System.out.println("1. ---" + userName);

        // 返回信息带上当前JVM所在机器的IP、进程号、时间
        String message = String.format(RESPONSE_TEMPLETE,
                userName,
                getIpAddress(),
                getPID(),
                new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss" ).format(new Date()));

        System.out.println("2. ---" + message);

        // 响应内容也是JSON格式，所以先存入map，然后再序列化
        Map<String, Object> rlt = new HashMap<>();
        rlt.put("success", true);
        rlt.put("message", message);

        String rltStr = null;

        try {
            rltStr = mapper.writeValueAsString(rlt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Response res = new Response();
        res.setContentType("application/json;charset=utf-8");
        res.setBody(rltStr);

	    return res;
    }
}
