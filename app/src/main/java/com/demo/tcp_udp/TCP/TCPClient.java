package com.demo.tcp_udp.TCP;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.tcp_udp.DataFormat;
import com.demo.tcp_udp.R;
import com.demo.tcp_udp.utils.CommonUtils;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.demo.tcp_udp.utils.CommonUtils.getInstance;

/**
 * Created by Administrator on 2016/10/7.
 */
public class TCPClient {
    private int port;
    private String serverIp;
    private boolean isConnect = true;
    private Socket Socket;
    private int count;
    private tcpSendRunnable sendRunnable;
    char[] buffer = new char[256];
    private byte[] rBuffer = new byte[1024];//接收数据缓存1024字节;
    private byte[] sBuffer;
    private TextView RDataWindow;
    private boolean isSHex = false;
    private boolean isRHex = false;
    private final static int SEND_ERRO = 1;
    private final static int RECEIVE = 2;
    private final static int CONNECT_ERRO = 3;
    private Context mContext;
    private String sRecvData;
    private boolean isTcpRecRunning = false;
    ArrayList<String> contentList=new ArrayList<String>();
    public TCPClient(Context Context, String serverIp, int port) {
        this.mContext = Context;
        this.serverIp = serverIp;
        this.port = port;
    }

    public MyHandler vhandler = new MyHandler();

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_ERRO:
                    log(mContext.getString(R.string.make_sure_input_right));
                    Toast.makeText(mContext, mContext.getString(R.string.make_sure_input_right), Toast.LENGTH_LONG).show();
                    break;
                case RECEIVE:
                    log("into receive handle.");
                    if (sRecvData != null) {
                        String str = sRecvData;
                        CommonUtils.getInstance().appendString(RDataWindow,str,contentList);
                        if (rBuffer != null) {
                            //iShowData.showData(rBuffer);
                            //Arrays.fill(rBuffer, (byte) 0);
                        }
                    }
                    Arrays.fill(rBuffer, (byte) 0);
                    sRecvData = null;//清空
                    break;
                case CONNECT_ERRO:
                    Toast.makeText(mContext,mContext.getResources().getString(R.string.can_not_find_server),Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }
    private void sendMessage2Handle(int msg) {
        Message message = new Message();
        message.what = msg;
        vhandler.sendMessage(message);
    }

    public void connectSocket() {
        //isListening = true;
        starRecThread();
    }

    private void starRecThread() {
        if (!isTcpRecRunning) {
            isConnect = true;
            new Thread(tcpRecRunnable).start();
        }
    }

    public void disConnectSocket() {
        //isListening = false;
        isConnect = false;
        if (Socket != null) {
            try {
                Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendData(byte[] packet) {
        String str = "";
        str = String.format("s:[%s]%s",
                new SimpleDateFormat("HH:mm:ss").format(new Date()), DataFormat.getInstance().byteToStr(packet));
        CommonUtils.getInstance().appendString(RDataWindow, str,contentList);
        sendRunnable = new tcpSendRunnable(packet);
        new Thread(sendRunnable).start();
    }

    public void sendData(String str) {
        sendRunnable = new tcpSendRunnable(str);
        new Thread(sendRunnable).start();
    }

    private void sendBufData(byte[] Buffer)//未验证
    {
        if (Socket != null) {
            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(Socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (dos!=null) {
                try {
                    dos.write(Buffer);
                    dos.flush();
                    // dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    sendMessage2Handle(SEND_ERRO);
                    loge(" Exception:" + e);//
                }
            }
        }
    }

    public void setRDataWindow(TextView RDataWindow) {
        if (RDataWindow != null) {
            this.RDataWindow = RDataWindow;
            this.RDataWindow.setMovementMethod(ScrollingMovementMethod.getInstance());//设置垂直滚动条
        }
    }

    public void sendStrData(String SData) {
        if (Socket!=null) {
            if (isSHex) {//发送16进制
                try {
                    sBuffer = DataFormat.getInstance().strToHex(SData);
                } catch (Exception e) {
                    e.printStackTrace();
                    loge("erro:" + e.getMessage());
                    sendMessage2Handle(SEND_ERRO);
                }
            } else {
                try {
                    sBuffer = SData.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    loge("Exception:" + e.getMessage());
                    sendMessage2Handle(SEND_ERRO);
                }
            }
            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(Socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                loge("e:" + e.getMessage());
            }
            if (dos != null) {
                try {
                    dos.write(sBuffer);
                    dos.flush();
                    log("send str:" + sBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    loge("e:"+e.getMessage());
                }
            }
        }
    }

    /*
* 函数：tcpSendRunnable
* 参数：参数有string和byte[]两类
* 功能：tcp发送线程
* */
    private class tcpSendRunnable implements Runnable {
        String content;
        byte[] buff;
        int type = 0;

        public tcpSendRunnable(String content) {
            this.content = content;
            type = 1;
        }

        public tcpSendRunnable(byte[] buff) {
            this.buff = buff;
            type = 2;
        }

        @Override
        public void run() {
            synchronized (this) {
                switch (type) {
                    case 1:
                        sendStrData(this.content);
                        break;
                    case 2:
                        sendBufData(this.buff);
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }

            }
        }
    }

    private Runnable tcpRecRunnable = new Runnable() {//接收runnable~~~~~~~~~~~~~~~~~~~~
        @Override
        public void run() {
            isTcpRecRunning = true;
            if (creatClientSocket()) {
                BufferedReader in = null; //获取输入流
                try {
                    in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (isConnect) {//获取从客户端发来的信息
                    recvData(in);
                }
            }
            isTcpRecRunning = false;
        }
    };

    private void recvData(BufferedReader in) {
        if (in != null) {
            try {
                count = in.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                loge(e.getMessage());
            }
            if (count > 0) {
                if (isRHex) {
                    byte[] byteBuf;
                    byteBuf = DataFormat.getInstance().char2Bytes(buffer);
                    sRecvData = DataFormat.getInstance().hexToStr(byteBuf, count);
                } else {
                    sRecvData = DataFormat.getInstance().char2Str(buffer, count);
                }
                sRecvData = String.format("[%s:%d//%s]%s", Socket.getInetAddress(), Socket.getPort(),
                        new SimpleDateFormat("HH:mm:ss").format(new Date()), sRecvData);
                sendMessage2Handle(RECEIVE);
            }
        }
    }

    private boolean creatClientSocket() {
        if (Socket == null) {
            try {
                Socket = new Socket(serverIp, port);
            } catch (IOException e) {
                e.printStackTrace();
                loge(e.getMessage());
                sendMessage2Handle(CONNECT_ERRO);
                return false;
            }
        }
        return true;
    }

    public void clearRecWindow()
    {
        RDataWindow.setText("");
        contentList.clear();
    }
    public void setIsSHex(boolean isSHex) {
        this.isSHex = isSHex;
    }

    public void setIsRHex(boolean isRHex) {
        this.isRHex = isRHex;
    }

    private void log(String str) {
        Log.i("chenxi", str + " @TCPServer");
    }

    private void loge(String str) {
        Log.e("chenxi", "line:" + CommonUtils.getInstance().getLineNumber(new Exception())+str + " @TCPServer");
    }
}
