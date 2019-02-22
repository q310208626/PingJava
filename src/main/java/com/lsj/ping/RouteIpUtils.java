package com.lsj.ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @descriptions:
 * @version: v1.0.0
 * @author: linsj3
 * @create: 2019-02-22 11:45
 **/

public class RouteIpUtils {

    public String getRouteIP() {
        try {
            Process pro = Runtime.getRuntime().exec("ipconfig");
            BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream(),"gb2312"));
            List<String> rowList = new ArrayList();
            String temp;
            while ((temp = br.readLine()) != null) {
                rowList.add(temp);
            }
            for (String string : rowList) {
//			    if(string.indexOf(/*"Subnet Mask"*/"子网掩码") != -1){
//			        Matcher mc = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").matcher(string);
//			        if(mc.find()){
//			            System.out.println("子掩码：" + mc.group());
//			        }else{
//			            System.out.println("子掩码为空");
//			        }
//			    };
                if (string.indexOf(/*"Default Gateway"*/"默认网关") != -1) {
                    Matcher mc = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").matcher(string);
                    if (mc.find()) {
                        System.out.println("默认网关：" + mc.group());
                        return mc.group();
                    } else {
                        continue;

                    }
                }
                ;
                if (string.indexOf("Default Gateway"/*"默认网关"*/) != -1) {
                    Matcher mc = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").matcher(string);
                    if (mc.find()) {
                        System.out.println("默认网关：" + mc.group());
                        return mc.group();
                    } else {
                        continue;
                    }
                };
            }

            System.out.println("默认网关为空");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}