package com.demo.tcp_udp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.tcp_udp.Interface.ISendInTime;
import com.demo.tcp_udp.R;
import com.demo.tcp_udp.SendInTime;
import com.demo.tcp_udp.TCP.TCPClient;

import java.io.BufferedReader;
import java.net.Socket;

/**
 * Created by Administrator on 2016/10/3.
 */

public class TCPClientFragment extends Fragment implements View.OnClickListener,ISendInTime{
    private View mView;
    private EditText etIPAddr;
    private EditText etPort;
    private TextView RDataWin;
    // SocketInfo socketInfo;
    Socket socket;
    BufferedReader br;
    private String getContent = "";
    private Thread thread = null;
    private EditText etSendContent;
    //private MyHander connectHandle = new MyHander();
    private TCPClient tcpClient;
    private CheckBox cbHexS;
    private CheckBox cbHexR;
    private Context mContext;
    private CheckBox cbCyclicSend;
    private EditText etCyclicTime;
    private int time;
    private SendInTime sendInTime=new SendInTime();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        this.mContext = this.getActivity();
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_tcp_client, container, false);
        init();
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tcpClient!=null)
            tcpClient.disConnectSocket();
    }

    private void init() {
        etIPAddr = (EditText) mView.findViewById(R.id.etIP);
        etPort = (EditText) mView.findViewById(R.id.etPort);
        RDataWin = (TextView) mView.findViewById(R.id.tvGet);
        etSendContent = (EditText) mView.findViewById(R.id.etSendContent);
        cbHexS = (CheckBox) mView.findViewById(R.id.cbHexS);
        cbHexS.setOnClickListener(this);
        cbHexR = (CheckBox) mView.findViewById(R.id.cbHexR);
        cbHexR.setOnClickListener(this);
        cbCyclicSend=(CheckBox)mView.findViewById(R.id.cbCyclicSend);
        cbCyclicSend.setOnClickListener(this);
        etCyclicTime=(EditText)mView.findViewById(R.id.etCyclicTime);
        //socketInfo=new SocketInfo();
        Button btSend = (Button) mView.findViewById(R.id.btSend);
        btSend.setOnClickListener(this);
        Button btClear = (Button) mView.findViewById(R.id.btClear);
        btClear.setOnClickListener(this);
        Switch swListen = (Switch) mView.findViewById(R.id.stConnect);
        swListen.setOnCheckedChangeListener(connectSwitch);
        sendInTime.setSendInTimeInterface(this);


    }

    private void initTCP() {
        String ip = null;
        int port = 0;
        if (etIPAddr.getText().toString().equals("")) {
            ip = etIPAddr.getHint().toString();
        } else {
            ip = etIPAddr.getText().toString();
        }
        if (etPort.getText().toString().equals("")) {
            port = Integer.valueOf(etPort.getHint().toString());
        } else {
            port = Integer.valueOf(etPort.getText().toString());
        }
        tcpClient = new TCPClient(mContext,ip, port);
        tcpClient.setRDataWindow(RDataWin);
        //udpClient.setShowDataListener(this);
    }

    CompoundButton.OnCheckedChangeListener connectSwitch = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                initTCP();
                tcpClient.connectSocket();
                etIPAddr.setEnabled(false);
                etPort.setEnabled(false);
            } else {
                tcpClient.disConnectSocket();
                tcpClient=null;
                etIPAddr.setEnabled(true);
                etPort.setEnabled(true);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btSend:
                InputMethodManager imm=(InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                sendContent();
                break;
            case R.id.cbHexS:
                if (tcpClient != null) {
                    if (cbHexS.isChecked()) {
                        tcpClient.setIsSHex(true);
                    } else {
                        tcpClient.setIsSHex(false);
                    }
                }
                break;
            case R.id.cbHexR:
                if (tcpClient != null) {
                    if (cbHexR.isChecked()) {
                        tcpClient.setIsRHex(true);
                    } else {
                        tcpClient.setIsRHex(false);
                    }
                }
                break;
            case R.id.cbCyclicSend:
                if (tcpClient != null) {
                    if (cbCyclicSend.isChecked()) {
                        if (etCyclicTime.getText().toString().equals("")) {
                            time = Integer.parseInt(etCyclicTime.getHint().toString());
                        } else {
                            time = Integer.parseInt(etCyclicTime.getText().toString());
                        }
                        sendInTime.starSendInTime(time);
                    } else {
                        sendInTime.stopSendInTime();
                    }
                }
                break;
            case R.id.btClear:
                if (tcpClient!=null){
                    tcpClient.clearRecWindow();
                }else {
                    RDataWin.setText("");
                }
                break;
            default:
                break;
        }
    }


    private void log(String str) {
        Log.i("chenxi", str + "  @TCPClientFragment");
    }

    private void tcpSend(String content){
        if (tcpClient!=null)
            tcpClient.sendData(content);
    }
    @Override
    public void sendDataInTime() {
        sendContent();
    }
    private void sendContent(){
        if (!"".equals(etSendContent.getText().toString())){
            tcpSend(etSendContent.getText().toString());
        }else {
            Toast.makeText(mContext,mContext.getResources().getString(R.string.send_content_can_not_null),Toast.LENGTH_LONG).show();
        }
    }
}
