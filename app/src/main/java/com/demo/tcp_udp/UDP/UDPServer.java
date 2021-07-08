package com.demo.tcp_udp.UDP;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.demo.tcp_udp.DataFormat;
import com.demo.tcp_udp.R;
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
import java.util.List;

import static com.demo.tcp_udp.utils.CommonUtils.getInstance;

/**
 * Created by Administrator on 2016/10/6.
 */
public class UDPServer implements AdapterView.OnItemSelectedListener {
    //private DatagramSocket socketUDP = null;
    private int remotePort, localPort;
    private DatagramPacket sPacket = null;
    private DatagramPacket rPacket = null;
    private ArrayAdapter<String> adapter;
    private byte[] rBuffer = new byte[1024];//接收数据缓存1024字节
    private byte[] sBuffer;
    private boolean isSHex = false;
    private boolean isRHex = false;
    private String sRecvData;
    private TextView RDataWindow;
    private Spinner spClientsIp;
    private final static int SENDERRO = 0;
    private final static int RECEIVE = 1;
    private final static int ADDRESSNULL = 2;
    private Context mContext;
    private udpSendRunnable usr;
    private boolean isConnect = false;
   // private boolean isUdpRecRunning = false;
    // private IShowData iShowData;
    private String remoteIP = null;
    private List<String> remoteIpList = new ArrayList<String>();
    private List<Integer> remotePortList = new ArrayList<Integer>();
    private ArrayList<String> contentList = new ArrayList<String>();
    public MyHandler vhandler = new MyHandler();
    private int clinetIndex = 0;
    private MulticastSocket multicastSocket;
    private static String BROADCAST_IP = "224.0.0.1";
    private static int BROADCAST_PORT;
    private InetAddress inetAddress;

    public UDPServer(Context Context) {
        this.mContext = Context;
        adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_dropdown_item, remoteIpList);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        clinetIndex = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    boolean isNewClientIp = false;

    public class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SENDERRO:
                    log(mContext.getString(R.string.make_sure_input_right));
                    Toast.makeText(mContext, mContext.getString(R.string.make_sure_input_right), Toast.LENGTH_LONG).show();
                    break;
                case RECEIVE:
                    //spClientsIp.setVisibility(View.VISIBLE);
                    if (remoteIpList.size() == 0) {
                        remotePortList.add(remotePort);
                        remoteIpList.add(remoteIP);
                        adapter.notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < remoteIpList.size(); i++) {
                         /*   log("remoteIpList.get(i):" + remoteIpList.get(i));
                            log("remoteIP:"+remoteIP);*/
                            if (!remoteIpList.get(i).toString().equals(remoteIP)) {
                                isNewClientIp = true;
                            } else {
                                isNewClientIp = false;
                                break;
                            }
                        }
                        if (isNewClientIp) {
                            remotePortList.add(remotePort);
                            remoteIpList.add(remoteIP);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(mContext, mContext.getString(R.string.updated_client_list), Toast.LENGTH_LONG).show();
                        }
                    }
                    log("remoteIpList size:" + remoteIpList.size());

                    if (sRecvData != null) {
                        String str = sRecvData;
                        CommonUtils.getInstance().appendString(RDataWindow, str, contentList);
                        if (rBuffer != null) {
                            //iShowData.showData(rBuffer);//showData这个程序中未使用，其他程序会使用到要保留
                        }
                    }
                    Arrays.fill(rBuffer, (byte) 0);
                    sRecvData = null;//清空
                    break;
                case ADDRESSNULL:
                    Toast.makeText(mContext, mContext.getString(R.string.client_send_first), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

    /*    public void setShowDataListener(IShowData Listener)
        {
            this.iShowData=Listener;
        }*/
    public boolean connectSocket() {
        boolean result = false;
        try {
           /* if (socketUDP == null) {
                socketUDP = new DatagramSocket(localPort);
            }*/
            if (rPacket == null)
                rPacket = new DatagramPacket(rBuffer, rBuffer.length);
            if (multicastSocket == null) {
                multicastSocket = new MulticastSocket(BROADCAST_PORT);
                inetAddress = InetAddress.getByName(BROADCAST_IP);
                //multicastSocket.setTimeToLive(1);
                multicastSocket.joinGroup(inetAddress);
            }
            startRec();
            result = true;
        } catch (SocketException se) {
            disConnectSocket();
            System.out.println("open udp port error:" + se.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void disConnectSocket() {
        isConnect = false;
        remotePortList.clear();
        remoteIpList.clear();
        adapter.notifyDataSetChanged();

    }

    private void closeSocket() {
        if (rPacket != null)
            rPacket = null;
        if (multicastSocket != null) ;
        {
            try {
                multicastSocket.leaveGroup(inetAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
            multicastSocket.close();
            multicastSocket = null;
        }
    }

    private void startRec() {
        //if (!isUdpRecRunning)
        {
            isConnect = true;
            new Thread(new udpRecRunnable()).start();
        }
    }

    public void SendStrData(String ipAddress, int port, String SData) {

        try {
            log("into sendstrdata");
            if (isSHex) {
                try {
                    sBuffer = DataFormat.getInstance().strToHex(SData);
                } catch (Exception e) {
                    e.printStackTrace();
                    loge("line:" + getInstance().getLineNumber(new Exception()) + "erro:" + e.getMessage());
                    sendMessage2Handle(SENDERRO);
                }
            } else {
                sBuffer = SData.getBytes("UTF-8");
            }
            log("remoteIP:" + remoteIP + ",remotePort" + remotePort);
            if (multicastSocket != null) {

                if ((ipAddress != null) && (port != 0)) {
                    try {
                       /* sPacket = new DatagramPacket(sBuffer, sBuffer.length,
                                InetAddress.getByName(remoteIpList.get(clinetIndex)),
                                remotePortList.get(clinetIndex));*/
                        //224.0.0.1为广播地址
                        log("ipAddress:" + ipAddress + ",port:" + port);
                        InetAddress address = InetAddress.getByName(ipAddress);
                        sPacket = new DatagramPacket(sBuffer, sBuffer.length, address,
                                port);
                        log("address:" + address + ",port:" + port);
                    } catch (Exception e) {
                        loge("line:" + getInstance().getLineNumber(new Exception()) + "Exception:" + e);
                    }
                    // socketUDP.send(sPacket);
                    multicastSocket.send(sPacket);
                } else {
                    sendMessage2Handle(ADDRESSNULL);
                }

            }
            sPacket = null;
        } catch (IOException ie) {
            if (multicastSocket != null) {
                multicastSocket.close();
                multicastSocket = null;
            }
            sPacket = null;
            loge("line:" + getInstance().getLineNumber(new Exception()) + "senddata error:" + ie.getMessage());
            sendMessage2Handle(SENDERRO);
        }
    }

    private void sendMessage2Handle(int msg) {
        Message message = new Message();
        message.what = msg;
        vhandler.sendMessage(message);
    }

    private void recvData() {
        if (multicastSocket!=null) {
            try {
                    multicastSocket.receive(rPacket);
            } catch (IOException e) {
                loge("line:" + getInstance().getLineNumber(new Exception()) + "recvdata error:" + e.getMessage());///************
                e.printStackTrace();
            }
            //socketUDP.receive(rPacket);
            remoteIP = rPacket.getAddress().getHostAddress();
            remotePort = rPacket.getPort();

            String currentRCodes = "UTF-8";
            if (isRHex) {
                //sRecvData = recvHexData(rPacket.getLength());
                sRecvData = DataFormat.getInstance().hexToStr(rBuffer, rPacket.getLength());
            } else {
                try {
                    sRecvData = new String(rPacket.getData(), currentRCodes).trim();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    loge("line:" + getInstance().getLineNumber(new Exception()) + "recvdata error:" + e.getMessage());
                }
            }
            sRecvData = String.format("[%s:%d//%s]%s", rPacket.getAddress().getHostAddress(), rPacket.getPort(),
                    new SimpleDateFormat("HH:mm:ss").format(new Date()), sRecvData);
        }
        /* catch (IOException ie) {
            loge("line:" + getInstance().getLineNumber(new Exception()) +  "recvdata error:" + ie.getMessage());
        }*/
    }

    public void sendData(byte[] packet) {
        String str = "";
        str = String.format("s:[%s]%s",
                new SimpleDateFormat("HH:mm:ss").format(new Date()), DataFormat.getInstance().byteToStr(packet));
        appendString(str);
        usr = new udpSendRunnable(remoteIpList.get(clinetIndex), remotePortList.get(clinetIndex), packet);
        new Thread(usr).start();
    }

    public void sendData(String str) {
        log("into senddata");
        if (remoteIpList.size() > 0) {
            usr = new udpSendRunnable(remoteIpList.get(clinetIndex), remotePortList.get(clinetIndex), str);
            new Thread(usr).start();
        } else {
            sendMessage2Handle(ADDRESSNULL);
        }
    }

    public void sendBroadcast(String str) {
        usr = new udpSendRunnable(BROADCAST_IP, BROADCAST_PORT, str);
        new Thread(usr).start();
    }

    private void sendBufData(String ipAddress, int port, byte[] Buffer) {
        //if (multicastSocket != null)
        {
            try {
                if (remoteIP != null) {
                    sPacket = new DatagramPacket(Buffer, Buffer.length, InetAddress.getByName(ipAddress), port);
                    //socketUDP.send(sPacket);
                    multicastSocket.send(sPacket);
                }
            } catch (Exception e) {
                loge("line:" + getInstance().getLineNumber(new Exception()) + " Exception:" + e);//
                Message message = new Message();
                message.what = SENDERRO;
                vhandler.sendMessage(message);
            }
        }
    }

    private int lineNum = 0;

    private void appendString(String str) {
        if (str != null) {
            if (RDataWindow != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.append("\n");
                sb.append(RDataWindow.getText().toString().trim());
                RDataWindow.setText(sb.toString().trim());
                sb.delete(0, sb.length());
                sb = null;
                lineNum++;
                if (lineNum > 20) {
                    lineNum = 0;
                    RDataWindow.setText("");//函数大于20行清零
                }
            }
        }
    }

    /*
   * 函数：udpSendRunnable
   * 参数：参数有string和byte[]两类
   * 功能：udp发送线程
   * */
    private class udpSendRunnable implements Runnable {
        String content;
        String ipAddress;
        int port;
        byte[] buff;
        int type = 0;

        public udpSendRunnable(String ipAddress, int port, String content) {
            this.content = content;
            this.ipAddress = ipAddress;
            this.port = port;
            type = 1;
        }

        public udpSendRunnable(String ipAddress, int port, byte[] buff) {
            this.buff = buff;
            this.ipAddress = ipAddress;
            this.port = port;
            type = 2;
        }

        @Override
        public void run() {
            synchronized (this) {
                switch (type) {
                    case 1:
                        SendStrData(ipAddress, port, this.content);
                        break;
                    case 2:
                        sendBufData(ipAddress, port, this.buff);
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }

            }
        }
    }

    public void clearRecWindow() {
        RDataWindow.setText("");
        contentList.clear();
    }

    private class udpRecRunnable implements Runnable {//udp接收线程

        @Override
        public void run() {
            log("udpRecRunnable");
            //isUdpRecRunning = true;
            log("isConnect=" + isConnect);
            while (isConnect) {
                if (isConnect) {
                    recvData();
                    sendMessage2Handle(RECEIVE);
                }
                //Thread.sleep(50);
            }
            closeSocket();
           // isUdpRecRunning = false;
        }
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
        BROADCAST_PORT=localPort;
        log("BROADCAST_PORT:"+BROADCAST_PORT);
    }

    public void setRHex(boolean RHex) {
        isRHex = RHex;
        log("isRHex:" + isRHex);
    }

    public void setSHex(boolean SHex) {
        isSHex = SHex;
    }

    public void setRDataWindow(TextView RDataWindow) {
        this.RDataWindow = RDataWindow;
        this.RDataWindow.setMovementMethod(ScrollingMovementMethod.getInstance());//设置垂直滚动条
    }

    public void setCleintsIpSpinner(Spinner spClientsIp) {
        if (spClientsIp != null) {
            this.spClientsIp = spClientsIp;
            spClientsIp.setOnItemSelectedListener(this);
            spClientsIp.setAdapter(adapter);
        }
    }

    private void log(String str) {
        Log.i("chenxi", str + " @UDPServer");
    }

    private void loge(String str) {
        Log.e("chenxi", str + " @UDPServer");
    }
}
