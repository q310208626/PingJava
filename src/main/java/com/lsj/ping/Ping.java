package com.lsj.ping;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @descriptions:
 * @version: v1.0.0
 * @author: linsj3
 * @create: 2019-02-22 11:46
 **/
public class Ping extends Frame {
    private TextArea pingTextArea;
    private Label pingLable;
    private JButton pingButton;
    private TextField pingTextField;
    private NetworkInterface[] devices; // 获取网卡设备
    private JpcapSender sender;
    private ICMPPacket icmpPacket;
    private JpcapCaptor jpcapCaptor;
    private EthernetPacket ethernetPacket;
    private int timeOut = 4000;// 超时时间
    private long sendTime = 0;// 记录发送时间
    private int catchMessage = 0;// 记录发送的报文数
    private boolean judgeCaptor = false;
    private String hostname = "";
    private int HostType = 0;
    private static String RouterIP;
    private String hostip;

    // private JpcapCaptor myCaptor=null;//获取目标所用的jpcaotor
    private static byte[] mydst_mac = null;// 目标mac

    private static Object lock = new Object();
    private static boolean flag = false;

    // CaptorThread captorThread = new CaptorThread();

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.library.path"));
        RouterIP=new RouteIpUtils().getRouteIP();//获取路由器ip
        try {
            mydst_mac=new FetchMac().getMacAddress(RouterIP);//由路由器ip获取mac
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(RouterIP!=null){
            Ping ping = new Ping();
            ping.addWindowListener(ping.new MyWindowAdapter());


        }
    }

    public Ping() {
        setBounds(200, 200, 600, 400);
        setVisible(true);
        setTitle("MyPingFunction");
        pingLable = new Label("Please type the IP");
        pingLable.setBounds(10, 40, 100, 20);
        pingTextField = new TextField();
        pingTextField.setBounds(120, 40, 200, 20);
        pingButton = new JButton("ping");
        pingButton.setBounds(330, 40, 60, 20);
        pingButton.addActionListener(new PingButtonListener());
        pingTextArea = new TextArea();
        pingTextArea.setBounds(40, 70, 540, 300);
        add(pingLable);
        add(pingTextField);
        add(pingButton);
        add(pingTextArea);

    }

    public class MyWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            setVisible(false);
            System.exit(0);
        }
    }

    public class PingButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (pingTextField.getText() == null
                    || pingTextField.getText().equals("")) {

            } else {
                hostname = pingTextField.getText().toString();
                pingFunction();
            }
        }

    }

    public InetAddress getHost() {
        String hostName1 = "^[w]{3}\\.\\w*\\..*"; // www.baidu.com
        String hostName2 = "(\\w*\\..*)"; // baidu.com
        String hostName3 = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"; // 14.215.177.38
        Pattern pattern1 = Pattern.compile(hostName1);
        Pattern pattern2 = Pattern.compile(hostName2);
        Pattern pattern3 = Pattern.compile(hostName3);
        Matcher m01 = pattern1.matcher(hostname);
        Matcher m02 = pattern2.matcher(hostname);
        Matcher m03 = pattern3.matcher(hostname);
        try {
            if (m01.find()) {
                HostType = 1;
                System.out.println(m01.group());
                System.out.println(InetAddress.getByName(hostname).toString());
                hostip=InetAddress.getByName(hostname).toString();
                return InetAddress.getByName(hostname);
            } else if (m03.find()) {
                HostType = 2;
                System.out.println(m03.group());
                hostip=hostname;
                return InetAddress.getByName(hostname);
            } else if (m02.find()) {
                HostType = 1;
                System.out.println(m02.group());
                hostip=InetAddress.getByName(hostname).toString();
                return InetAddress.getByName(hostname);
            }
        } catch (IOException x) {
            HostType = 0;
            System.out.println("未知主机名");
        }
        return null;
    }

    public void pingFunction() {
        getHost();
        devices = JpcapCaptor.getDeviceList();
        // 获取到主机名执行以下代码
        if (HostType != 0) {
            //System.out.println("正在 Ping "+hostip+" 具有 32 字节的数据");
            pingTextArea.append("正在 Ping "+hostip+" 具有 32 字节的数据\n");
            packageSendAndCatch();
        }
        // 获取不到主机名执行以下代码
        else {
            pingTextArea.append("未知主机名" + "\n");
            System.out.println("未知主机名");
        }
    }

    public void packageSendAndCatch() {
        try {
            jpcapCaptor = JpcapCaptor.openDevice(devices[0], 1000, false, 1000);
            sender = jpcapCaptor.getJpcapSenderInstance(); // 建立Packet发送器
            jpcapCaptor.setFilter("icmp", true);// 设置接受报文类型
            icmpPacket = new ICMPPacket();
            icmpPacket.type = ICMPPacket.ICMP_ECHO;// 根据icmp ping 应选Echo类型
            icmpPacket.setIPv4Parameter(0, false, false, false, 0, false,
                    false, false, 0, 1010101, 100, IPPacket.IPPROTO_ICMP,
                    devices[0].addresses[1].address,
                    InetAddress.getByName(hostname));
            icmpPacket.data = "abcdefghijklmnopqrstuvwxyzabcdef".getBytes(); // 32位数据
            ethernetPacket = new EthernetPacket();
            ethernetPacket.frametype = EthernetPacket.ETHERTYPE_IP;
            ethernetPacket.src_mac = devices[0].mac_address;// 本机网卡mac
            ethernetPacket.dst_mac = mydst_mac;//目的mac
            icmpPacket.datalink = ethernetPacket;
            // 发送4次报文，每秒一个
            Thread sendThread = new Thread() {
                int sendamount = 0;

                @Override
                public void run() {
                    for (int i = 0; i < 4; i++) {
                        synchronized (lock) {
                            icmpPacket.sec = 0;
                            icmpPacket.usec = new GregorianCalendar()
                                    .getTimeInMillis();// 获取发送时间
                            icmpPacket.seq = (short) (1000 + i); // 序号由1000开始
                            icmpPacket.id = (short) (999 + i);
                            sendTime = System.currentTimeMillis();
                            sender.sendPacket(icmpPacket); // 发送包
                            System.out.println("send:" + i);
                            try {
                                lock.notify();
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            };
            Thread captorThread = new Thread() {
                public void run() {
                    synchronized (lock) {
                        while (true) {
                            if (System.currentTimeMillis() - sendTime < timeOut) {
                                ICMPPacket catchedIcmpPacket = null;
                                long time = 0;
                                String timeString = null;
                                catchedIcmpPacket = (ICMPPacket) jpcapCaptor.getPacket();
                                if ((catchedIcmpPacket != null)
                                        && (catchedIcmpPacket.seq
                                        - catchedIcmpPacket.id == 1)
                                        && (catchedIcmpPacket.type == ICMPPacket.ICMP_ECHOREPLY)) {

                                    time = (catchedIcmpPacket.sec * 1000
                                            + catchedIcmpPacket.usec / 1000
                                            - icmpPacket.sec * 1000 - icmpPacket.usec);
                                    if (time <= 0)
                                        timeString = "<1ms";
                                    else
                                        timeString = "=" + time + "ms";
                                    System.out.println("来自 "
                                            + catchedIcmpPacket.src_ip
                                            .getHostAddress() + " 的回复："
                                            + "字节="
                                            + catchedIcmpPacket.data.length
                                            + " 时间" + timeString + " TTL="
                                            + catchedIcmpPacket.hop_limit);
                                    pingTextArea.append("来自 "
                                            + catchedIcmpPacket.src_ip
                                            .getHostAddress() + " 的回复："
                                            + "字节="
                                            + catchedIcmpPacket.data.length
                                            + " 时间" + timeString + " TTL="
                                            + catchedIcmpPacket.hop_limit
                                            + "\n");
                                    catchMessage++;
                                    try {
                                        lock.notifyAll();
                                        lock.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                if (catchMessage < 5) {
                                    System.out.println("请求超时");
                                    catchMessage++;
                                    pingTextArea.append("请求超时" + "\n");
                                    try {
                                        lock.notifyAll();
                                        lock.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else
                                    break;
                            }
                        }
                    }
                };
            };
            sendThread.start();
            captorThread.start();
        } catch (IOException e) {
            System.out
                    .println("网卡出错或者jpcapCaptor.setFilter出错或setIPv4Parameter出错");
        }

    }
}

