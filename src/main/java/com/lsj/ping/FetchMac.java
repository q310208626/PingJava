package com.lsj.ping;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @descriptions:
 * @version: v1.0.0
 * @author: linsj3
 * @create: 2019-02-22 11:46
 **/

public class FetchMac {
    public String command(String cmd) throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
        InputStream in = process.getInputStream();
        StringBuilder result = new StringBuilder();
        byte[] data = new byte[256];
        while (in.read(data) != -1) {
            String encoding = System.getProperty("sun.jnu.encoding");
            result.append(new String(data, encoding));
        }
        return result.toString();
    }


    /**
     * 获取mac地址
     *
     * @param ip
     * @return
     * @throws Exception
     */
    public byte[] getMacAddress(String ip) throws Exception {
        String result = command("ping " + ip + " -n 2");
        if (result.contains("TTL")) {
            result = command("arp -a " + ip);
        }
        String regExp = "([0-9A-Fa-f]{2})([-:][0-9A-Fa-f]{2}){5}";
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(result);
        StringBuilder mac = new StringBuilder();
        while (matcher.find()) {
            String temp = matcher.group();
            mac.append(temp);
        }
        return getMacBytes(mac.toString());
    }

    //把String类型mac转成byte[]数组
    public byte[] getMacBytes(String mac) {
        byte[] macBytes = new byte[6];
        String[] strArr = mac.split("-");

        for (int i = 0; i < strArr.length; i++) {
            int value = Integer.parseInt(strArr[i], 16);
            macBytes[i] = (byte) value;
        }
        return macBytes;
    }

}
