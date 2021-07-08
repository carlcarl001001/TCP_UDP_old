package com.demo.tcp_udp.UDP;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.tcp_udp.DataFormat;
import com.demo.tcp_udp.Interface.IShowData;
import com.demo.tcp_udp.utils.CommonUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


/**
 * Created by innovpower on 2016/4/13.
 */
public class UDPClient{
    private String serverIP;
    private int serverPort, localPort;
    //private DatagramSocket socketUDP = null;
    private DatagramPacket sPacket = null;
    private DatagramPacket rPacket = null;
    private boolean isSHex = false;
    private boolean isRHex = false;
    private String sRecvData;
    private byte[] rBuffer = new byte[1024];//接收数据缓存1024字节;
    private byte[] sBuffer;
    private TextView RDataWindow;
    private Context mContext;
    private final static int SENDERRO = 1;
    private final static int RECEIVE = 2;
    private udpSendRunnable usr;
    private IShowData iShowData;
    private boolean isConnect=false;
    private boolean isUdpRecRunning =false;
    private MulticastSocket multicastSocket=null;
    private static int BROADCAST_PORT;
    private static String BROADCAST_IP="224.0.0.1";
    InetAddress inetAddress=null;
    private ArrayList<String> contentList = new ArrayList<String>();
    public UDPClient(Context Context) {
        this.mContext = Context;
    }

    public MyHandler vhandler = new MyHandler();
    public class MyHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SENDERRO:
                    log("请确认输入正确");
                    Toast.makeText(mContext, "请确认输入正确", Toast.LENGTH_LONG).show();
                    break;
                case RECEIVE:
                    log("into receive handle.");
                    if (sRecvData!=null)
                    {
                        String str = sRecvData;
                        CommonUtils.getInstance().appendString(RDataWindow, str,contentList);
                        if (rBuffer!=null) {
                            //iShowData.showData(rBuffer);//showData这个程序中未使用，其他程序会使用到要保留
                            //Arrays.fill(rBuffer, (byte) 0);
                        }
                    }
                    Arrays.fill(rBuffer, (byte) 0);
                    sRecvData=null;//清空
                    break;
                default:
                    break;
            }
        }
    };
    public void setShowDataListener(IShowData Listener)
    {
        this.iShowData=Listener;
    }

    public boolean connectSocket() {
        log("connectSocket");
        boolean result = false;
        try {
/*            if (socketUDP == null) {
                socketUDP = new DatagramSocket(localPort);
                log("new socketUDP localPort:"+localPort);
            }*/
            if (rPacket == null) {
                rPacket = new DatagramPacket(rBuffer, rBuffer.length);
            }
            if (multicastSocket==null) {
                multicastSocket = new MulticastSocket(BROADCAST_PORT);
                inetAddress = InetAddress.getByName(BROADCAST_IP);
                multicastSocket.joinGroup(inetAddress);
            }
            startRec();
            result = true;
        } catch (SocketException se) {
            disConnectSocket();
            System.out.println("open udp port error:" + se.getMessage());
            loge("line:" + CommonUtils.getInstance().getLineNumber(new Exception()) +
                    "open udp port error:" + se.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void disConnectSocket() {
        if (multicastSocket != null) {
            multicastSocket.close();
            multicastSocket = null;
        }
        if (rPacket != null)
            rPacket = null;
        isConnect=false;
    }

    private void startRec() {
        if (!isUdpRecRunning) {
            isConnect=true;
            new Thread(new udpRecRunnable()).start();
        }
    }
    private void recvData() {
        try {
            if (multicastSocket!=null) {
                multicastSocket.receive(rPacket);//UDP接收数据
                if (isRHex)
                    sRecvData=DataFormat.getInstance().hexToStr(rBuffer,rPacket.getLength());
                else
                    sRecvData = new String(rPacket.getData(), "UTF-8").trim();

                sRecvData =  String.format("[%s:%d//%s]%s", rPacket.getAddress().getHostAddress(), rPacket.getPort(),
                        new SimpleDateFormat("HH:mm:ss").format(new Date()), sRecvData);
            }
        } catch (IOException ie) {
            System.out.println("recvdata error:" + ie.getMessage());
            loge("recvdata error:" + ie.getMessage());
            // Toast.makeText(mContext, "接收数据错误:" + ie.getMessage(), Toast.LENGTH_LONG);
        }
    }

    public void sendStrData(String ipAddress, int port,String SData) {
        if (isSHex) {//发送16进制
            try {
                sBuffer= DataFormat.getInstance().strToHex(SData);
            } catch (Exception e) {
                e.printStackTrace();
                loge("line:" + CommonUtils.getInstance().getLineNumber(new Exception()) +
                        "erro:" + e.getMessage());
                sendMessage2Handle(SENDERRO);
            }
            log("hex send.");
        } else {
            try {
                sBuffer = SData.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                loge("line:" + CommonUtils.getInstance().getLineNumber(new Exception()) +
                        "Exception:" + e.getMessage());
                sendMessage2Handle(SENDERRO);
            }
            log("char send.");
        }
        if (multicastSocket!=null)
        {
            try {
                log("ipAddress:"+ipAddress+",port:"+port);
                log("serverIP:"+ serverIP +",serverPort:"+ serverPort);
                //sPacket = new DatagramPacket(sBuffer, sBuffer.length,InetAddress.getByName(ipAddress), port);
                InetAddress address = InetAddress.getByName(ipAddress);
                sPacket = new DatagramPacket(sBuffer, sBuffer.length, address,port);
                //sPacket = new DatagramPacket(sBuffer, sBuffer.length,InetAddress.getByName(serverIP), serverPort);
                //socketUDP.send(sPacket);
                multicastSocket.send(sPacket);
            } catch (Exception e) {
                loge("line:" + CommonUtils.getInstance().getLineNumber(new Exception()) +
                        "Exception:" + e.getMessage());
                sendMessage2Handle(SENDERRO);
            }
        }
        sPacket = null;

    }
    public void sendData(byte[] packet)
    {
        String str="";
        str=String.format("s:[%s]%s",new SimpleDateFormat("HH:mm:ss").format(new Date()),
                DataFormat.getInstance().byteToStr(packet));
        CommonUtils.getInstance().appendString(RDataWindow, str,contentList);
        usr = new udpSendRunnable(serverIP, serverPort,packet);
        new Thread(usr).start();
    }
    public void sendData(String str)
    {
        usr = new udpSendRunnable(serverIP, serverPort,str);
        new Thread(usr).start();
    }
    public void sendBroadcast(String str) {
        usr = new udpSendRunnable(BROADCAST_IP, BROADCAST_PORT, str);
        new Thread(usr).start();
    }
    private void sendBufData(String ipAddress, int port,byte[] Buffer)
    {
        if (multicastSocket!=null)
        {
            try {
                sPacket = new DatagramPacket(Buffer, Buffer.length, InetAddress.getByName
                        (ipAddress), port);
                //socketUDP.send(sPacket);
                multicastSocket.send(sPacket);
            } catch (Exception e) {
                loge("line:" + CommonUtils.getInstance().getLineNumber(new Exception()) +
                        " Exception:" + e);//
                sendMessage2Handle(SENDERRO);
            }
        }
    }
    private void sendBufData(byte[] Buffer)
    {
        if (multicastSocket!=null) {
            try {
                sPacket = new DatagramPacket(Buffer, Buffer.length, InetAddress.getByName(serverIP), serverPort);
                multicastSocket.send(sPacket);
            } catch (Exception e) {
                loge(" Exception:" + e);//
                sendMessage2Handle(SENDERRO);
            }
        }
    }
    public void clearRecWindow()
    {
        RDataWindow.setText("");
        contentList.clear();
    }
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
        log("serverIP:"+ serverIP);
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
        BROADCAST_PORT=serverPort;
        log("serverPort:"+ serverPort);
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
        log("localPort:"+localPort);
    }

    public void setRHex(boolean RHex) {
        isRHex = RHex;
        log("isRHex:"+isRHex);
    }

    public void setSHex(boolean SHex) {
        isSHex = SHex;
        log("isSHex:"+isSHex);
    }
    /*
    * 函数：udpSendRunnable
    * 参数：参数有string和byte[]两类
    * 功能：udp发送线程
    * */
    private class udpSendRunnable implements Runnable {
        String ipAddress;
        String content;
        byte[] buff;
        int type=0;
        int port;
        public udpSendRunnable(String ipAddress, int port, String content) {
            this.content = content;
            this.ipAddress = ipAddress;
            this.port = port;
            type=1;
        }
        public udpSendRunnable(String ipAddress, int port, byte[] buff) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.buff = buff;
            type=2;
        }
        @Override
        public void run() {
            synchronized (this) {
                switch (type)
                {
                    case 1:
                        sendStrData(ipAddress, port,this.content);
                        break;
                    case 2:
                        sendBufData(ipAddress, port,this.buff);
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }

            }
        }
    }
    private class udpRecRunnable implements Runnable {//udp接收线程

        @Override
        public void run() {
            isUdpRecRunning =true;
            while (isConnect) {
                try {
                    if (isConnect) {
                        recvData();
                        sendMessage2Handle(RECEIVE);
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isUdpRecRunning =false;
        }
    }
    public void setRDataWindow(TextView RDataWindow) {
        if (RDataWindow!=null) {
            this.RDataWindow = RDataWindow;
            this.RDataWindow.setMovementMethod(ScrollingMovementMethod.getInstance());//设置垂直滚动条
        }
    }
    private void sendMessage2Handle(int msg) {
        Message message = new Message();
        message.what = msg;
        vhandler.sendMessage(message);
    }
    private void log(String str) {
        Log.i("chenxi", str + "  @UDPClient");
    }
    private void loge(String str) {
        Log.e("chenxi","line:" + CommonUtils.getInstance().getLineNumber(new Exception()) +  str + "  @UDPClient");
    }
}
