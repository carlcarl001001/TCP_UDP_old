package com.demo.tcp_udp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.tcp_udp.Interface.ISendInTime;
import com.demo.tcp_udp.R;
import com.demo.tcp_udp.SendInTime;
import com.demo.tcp_udp.TCP.TCPServer;
import com.demo.tcp_udp.UDP.UDPServer;
import com.demo.tcp_udp.utils.CommonUtils;

/**
 * Created by Administrator on 2016/10/3.
 */
public class UDPServerFragment extends Fragment implements View.OnClickListener, ISendInTime {
    private Context mContext;
    private View mView;
    private UDPServer udpServer;
    private EditText etSendContent;
    private CheckBox isHexS;
    private CheckBox isHexR;
    private TextView RDataWin;
    private String IP;
    private EditText etPort;
    private CheckBox cbCyclicSend;
    private EditText etCyclicTime;
    private Spinner spClientsIp;
    private int time;
    private SendInTime sendInTime = new SendInTime();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this.getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_udp_server, container, false);
        init();
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (udpServer != null)
            udpServer.disConnectSocket();
    }

    private void init() {
        udpServer = new UDPServer(mContext);
        TextView tvIp = (TextView) mView.findViewById(R.id.tvIp);
        etPort = (EditText) mView.findViewById(R.id.etPort);
        etSendContent = (EditText) mView.findViewById(R.id.etSendContent);
        isHexS = (CheckBox) mView.findViewById(R.id.cbHexS);
        isHexS.setOnClickListener(this);
        isHexR = (CheckBox) mView.findViewById(R.id.cbHexR);
        isHexR.setOnClickListener(this);
        Button btSend = (Button) mView.findViewById(R.id.btSend);
        btSend.setOnClickListener(this);
        Button btClear = (Button) mView.findViewById(R.id.btClear);
        btClear.setOnClickListener(this);
        Button btBroadcast = (Button)mView.findViewById(R.id.btBroadcast);
        btBroadcast.setOnClickListener(this);
        IP = CommonUtils.getInstance().getLocalIP();
        tvIp.setText(IP);
        // getResources().getString(R.string.defPort);
       // tvPort.setText(getResources().getString(R.string.defPort));
       // int port = Integer.valueOf(getResources().getString(R.string.defPort));
       // udpServer.setLocalPort(port);
        Switch swListen = (Switch) mView.findViewById(R.id.swListen);
        swListen.setOnCheckedChangeListener(clickListen);
        RDataWin = (TextView) mView.findViewById(R.id.tvGet);
        udpServer.setRDataWindow(RDataWin);
        sendInTime.setSendInTimeInterface(this);
        cbCyclicSend = (CheckBox) mView.findViewById(R.id.cbCyclicSend);
        cbCyclicSend.setOnClickListener(this);
        etCyclicTime=(EditText)mView.findViewById(R.id.etCyclicTime);
        spClientsIp =(Spinner)mView.findViewById(R.id.spClientsIp);
        udpServer.setCleintsIpSpinner(spClientsIp);
        // udpServer.setShowDataListener(this);
        log("IP:" + IP + " port:" + getResources().getString(R.string.defPort));
    }

    CompoundButton.OnCheckedChangeListener clickListen = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                int port;
                if (etPort.getText().toString().equals("")) {
                    port = Integer.valueOf(etPort.getHint().toString());
                } else {
                    port = Integer.valueOf(etPort.getText().toString());
                }
                udpServer.setLocalPort(port);
                udpServer.connectSocket();
                etPort.setEnabled(false);
            } else {
                etPort.setEnabled(true);
                udpServer.disConnectSocket();
            }
        }
    };

    private void log(String str) {
        Log.i("chenxi", str + " @UDPServerFragment");
    }

    private void loge(String str) {
        Log.e("chenxi", str + " @UDPServerFragment");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btSend:
                closeInputMethod();
                sendContent();
                break;
            case R.id.btBroadcast:
                udpSendBroadcast(etSendContent.getText().toString());
                break;
            case R.id.cbHexR:
                if (udpServer != null) {
                    if (isHexR.isChecked()) {
                        udpServer.setRHex(true);
                    } else {
                        udpServer.setRHex(false);
                    }
                }
                break;
            case R.id.cbHexS:
                if (udpServer != null) {
                    if (isHexS.isChecked()) {
                        udpServer.setSHex(true);
                    } else {
                        udpServer.setSHex(false);
                    }
                }
                break;
            case R.id.cbCyclicSend:
                closeInputMethod();
                if (udpServer != null) {
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
                if (udpServer!=null){
                    udpServer.clearRecWindow();
                }else {
                    RDataWin.setText("");
                }
                break;
            default:
                break;
        }
    }

    private void udpSend(String content) {
        if (udpServer != null) {
            udpServer.sendData(content);
        }
    }
    private void udpSendBroadcast(String content) {
        if (udpServer != null) {
            udpServer.sendBroadcast(content);
        }
    }
    @Override
    public void sendDataInTime() {
        sendContent();
    }
    private void sendContent(){
        if (!"".equals(etSendContent.getText().toString())){
            udpSend(etSendContent.getText().toString());
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.send_content_can_not_null), Toast.LENGTH_LONG).show();
        }
    }
    private void closeInputMethod() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
            imm.hideSoftInputFromWindow(mView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
