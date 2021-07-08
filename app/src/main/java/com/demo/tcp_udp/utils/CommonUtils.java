package com.demo.tcp_udp.utils;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.demo.tcp_udp.Interface.ISendInTime;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * Created by Administrator on 2016/10/21.
 */

public class CommonUtils {
    //private ArrayList<String> contentList=new ArrayList<String>();
    String content;
    private final int CONTENTLINE = 40;//接收数据窗口保存数据行数

    public static CommonUtils getInstance() {
        return new CommonUtils();
    }

    public void appendString(TextView RDataWindow, String str, ArrayList<String> list) {
        String contentStr;
        if (str != null) {
            if (RDataWindow != null) {
                list.add(str);
                if (list.size() > CONTENTLINE) {
                    list.remove(0);
                }
                log("list.size():" + list.size());
                contentStr = list.get(0);
                for (int i = 1; i < list.size(); i++) {
                    contentStr = list.get(i) + "\n" + contentStr;
                }
                RDataWindow.setText(contentStr);
                contentStr="";
                str="";
            }
        }
    }

    public String getLocalIP() {
        String ipaddress = "网络连接异常";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
                        ipaddress = ip.getHostAddress();
                        return ipaddress;//有手机时应当把移动数据关闭
                        //不然会有两个ip
                    }
                }

            }
        } catch (SocketException e) {
            Log.e("chenxi", "获取本地ip地址失败");
            e.printStackTrace();
            ipaddress = "获取本地ip地址失败";
        }
        return ipaddress;
    }

    /**
     * 得到Exception所在代码的行数
     * 如果没有行信息,返回-1
     */
    public int getLineNumber(Exception e) {
        StackTraceElement[] trace = e.getStackTrace();
        if (trace == null || trace.length == 0) return -1; //
        return trace[0].getLineNumber();
    }

    private void log(String str) {
        Log.i("chenxi", str + " @CommonUtils");
    }

}
