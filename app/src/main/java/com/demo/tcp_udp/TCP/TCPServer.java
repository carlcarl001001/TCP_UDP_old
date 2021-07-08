package com.demo.tcp_udp.TCP;

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


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.demo.tcp_udp.utils.CommonUtils.getInstance;

/**
 * Created by chenxi on 2016/1/25.
 */
public class TCPServer implements AdapterView.OnItemSelectedListener{
    private Thread thread = null;//控制小车线程
    private int port;
    private boolean isConnect = true;
    private ServerSocket serverSocket;
    //private Socket otherSocket;
    private List<Socket> otherSockets=new ArrayList<>();
    //static BufferedReader mBufferedReaderServer = null;
    private int count;
    char[] buffer = new char[256];
    private final static int SENDERRO = 1;
    private final static int RECEIVE = 2;
    private final static int ADDRESSNULL = 3;
    private final static int CLIENTCONNECT=4;
    private Context mContext;
    private String sRecvData;
    private byte[] sBuffer;
    private byte[] rBuffer = new byte[1024];//接收数据缓存1024字节;
    private boolean isSHex = false;
    private boolean isRHex = false;
    private TextView RDataWindow;
    private Spinner spClientsIp;
    private tcpSendRunnable sendRunnable;
    private ArrayList<String> contentList = new ArrayList<String>();
    private List<String> list = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private BufferedReader bufferedReader = null;
    private int clinetIndex=0;
    public MyHandler vhandler = new MyHandler();


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        clinetIndex=i;
        new Thread(tcpRecRunnable).start();
/*        if (bufferedReader !=null) {
            log("bufferedReader!=Null");
        }
        else {
            log("bufferedReader==Null");
        }
        log("i:" + i);*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SENDERRO:
                    log(mContext.getString(R.string.make_sure_input_right));
                    Toast.makeText(mContext, mContext.getString(R.string.make_sure_input_right), Toast.LENGTH_LONG).show();
                    break;
                case RECEIVE:
                    log("into receive handle.");
                    if (sRecvData != null) {
                        String str = sRecvData;
                        CommonUtils.getInstance().appendString(RDataWindow, str, contentList);
                        if (rBuffer != null) {
                            //iShowData.showData(rBuffer);
                            //Arrays.fill(rBuffer, (byte) 0);
                        }
                    }
                    Arrays.fill(rBuffer, (byte) 0);
                    sRecvData = null;//清空
                    break;
                case ADDRESSNULL:
                    Toast.makeText(mContext, mContext.getString(R.string.remote_address_null), Toast.LENGTH_LONG).show();
                    break;
                case CLIENTCONNECT:
                    //spClientsIp.setVisibility(View.VISIBLE);
                   // String clientIp=otherSockets.get(otherSockets.size()-1).getInetAddress().toString();

                    Toast.makeText(mContext, mContext.getString(R.string.updated_client_list), Toast.LENGTH_LONG).show();
                    list.add(otherSockets.get(otherSockets.size() - 1).getInetAddress().toString());
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }

    public TCPServer(Context context, int port) {
        this.mContext = context;
        this.port = port;
        adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_dropdown_item, list);
    }

    public void connectSocket() {
        //isListening = true;
        isConnect = true;
        thread = new Thread(tcpConnectRunnable);
        thread.start();

    }

    public void disconnectSocket() {
        //isListening = false;
        spClientsIp.setVisibility(View.INVISIBLE);
        isConnect = false;

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }
        list.clear();
        adapter.notifyDataSetChanged();
/*        if (otherSocket!=null){
            try {
                otherSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //otherSocket=null;
        }*/
    }

    public void sendData(byte[] packet) {
        String str = "";
        str = String.format("s:[%s]%s", new SimpleDateFormat("HH:mm:ss").format(new Date()), DataFormat.getInstance().byteToStr(packet));
        CommonUtils.getInstance().appendString(RDataWindow, str, contentList);
        sendRunnable = new tcpSendRunnable(packet);
        new Thread(sendRunnable).start();
    }

    public void sendData(String str) {
        sendRunnable = new tcpSendRunnable(str);
        new Thread(sendRunnable).start();
    }

    private void sendBufData(byte[] Buffer)//未验证
    {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(otherSockets.get(clinetIndex).getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (dos != null) {
            try {
                dos.write(Buffer);
                dos.flush();
                // dos.close();
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage2Handle(SENDERRO);
                loge("line:"+getInstance().getLineNumber(new Exception())+" Exception:" + e);//
            }
        }
    }

    public void sendStrData(String SData) {
        log("clinetIndex:"+clinetIndex);
        log("otherSockets.size():"+otherSockets.size());
        if (otherSockets.size()!= 0) {
            if (isSHex) {//发送16进制
                try {
                    sBuffer = DataFormat.getInstance().strToHex(SData);
                } catch (Exception e) {
                    e.printStackTrace();
                    loge("line:"+getInstance().getLineNumber(new Exception())+"erro:" + e.getMessage());
                    sendMessage2Handle(SENDERRO);
                }
            } else {
                try {
                    sBuffer = SData.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    loge("line:"+getInstance().getLineNumber(new Exception())+" Exception:" + e.getMessage());
                    sendMessage2Handle(SENDERRO);
                }
            }

            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(otherSockets.get(clinetIndex).getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (dos != null) {
                try {
                    dos.write(sBuffer);
                    dos.flush();
                    log("send str:" + sBuffer);
                } catch (IOException e) {//下线通知 有问题**********************
                    e.printStackTrace();
                }
            }
        } else {
            sendMessage2Handle(ADDRESSNULL);
            log("ADDRESSNULL");
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

    private Runnable tcpConnectRunnable = new Runnable() {
        @Override
        public void run() {
            //BufferedReader bufferedReader = null;
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
                loge("line:"+getInstance().getLineNumber(new Exception())+" IOException e:" + e.getMessage());
            }
            while (isConnect) {
                if (isConnect) {
                    if (createConnect()) {//这里要循环侦听
                        new Thread(tcpRecRunnable).start();
                    }
                }
            }
        }
    };

    private Runnable tcpRecRunnable=new Runnable() {

        @Override
        public void run() {
            while (isConnect) {
                if (isConnect) {
                    //获取输入流
                    try {
                        bufferedReader = new BufferedReader(
                                new InputStreamReader(otherSockets.get(clinetIndex).getInputStream()));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //获取从客户端发来的信息

                    if (otherSockets.get(clinetIndex) != null) {
                        recvData(bufferedReader);
                    }
                }
            }
        }
    };
    private void recvData(BufferedReader in) {
        if (in != null) {
            try {
                count = in.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                loge("line:"+getInstance().getLineNumber(new Exception())+" IOException:"+e.getMessage());
            }
            if (count > 0) {
                log("isRHex:" + isRHex);
                if (isRHex) {
                    byte[] byteBuf;
                    byteBuf = DataFormat.getInstance().char2Bytes(buffer);
                    sRecvData = DataFormat.getInstance().hexToStr(byteBuf, count);
                } else {
                    sRecvData = DataFormat.getInstance().char2Str(buffer, count);
                }
                sRecvData = String.format("[%s:%d//%s]%s", otherSockets.get(clinetIndex).getInetAddress(), otherSockets.get(clinetIndex).getPort(),
                        new SimpleDateFormat("HH:mm:ss").format(new Date()), sRecvData);
                sendMessage2Handle(RECEIVE);
            }
        }
    }

    private boolean createConnect() {
        log("into createConnect.");
/*        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            loge("IOException e:" + e.getMessage());
            return false;
        }*/
        try {
            Socket otherSocket = serverSocket.accept();//阻塞监听
            otherSockets.add(otherSocket);
            sendMessage2Handle(CLIENTCONNECT);
            log("somebody connect.");
        } catch (IOException e1) {
            loge("line:"+getInstance().getLineNumber(new Exception())+" IOException e1:" + e1);
            e1.printStackTrace();
            return false;
        }
        return true;
    }

    private String getInfoBuff(char[] buff, int count) {
        char[] temp = new char[count];
        for (int i = 0; i < count; i++) {
            temp[i] = buff[i];
        }
        return new String(temp);
    }

    private void sendMessage2Handle(int msg) {
        Message message = new Message();
        message.what = msg;
        vhandler.sendMessage(message);
    }

    public void setRDataWindow(TextView RDataWindow) {
        if (RDataWindow != null) {
            this.RDataWindow = RDataWindow;
            this.RDataWindow.setMovementMethod(ScrollingMovementMethod.getInstance());//设置垂直滚动条
        }
    }
    public void setCleintsIpSpinner(Spinner spClientsIp) {
        if (spClientsIp != null) {
            this.spClientsIp = spClientsIp;
            spClientsIp.setOnItemSelectedListener(this);
            spClientsIp.setAdapter(adapter);
        }
    }
    public void clearRecWindow() {
        contentList.clear();
        RDataWindow.setText("");
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
        Log.e("chenxi", str + " @TCPServer");
    }
}
