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
import com.demo.tcp_udp.UDP.UDPClient;

/**
 * Created by chenxi on 2016/9/29.
 */
public class UDPClientFragment extends Fragment implements View.OnClickListener ,ISendInTime{
    private Button btSend;
    private Switch stConnect;
    private EditText etSendContent;
    private CheckBox isHexS;
    private CheckBox isHexR;
    private View mView;
    private Context mContext;
    private TextView RDataWin;
    private EditText etIP;
    private EditText etRPort;
    //private EditText etLPort;
    private CheckBox cbCyclicSend;
    private EditText etCyclicTime;
    private int time;
    private SendInTime sendInTime=new SendInTime();
    //private UDPServerCopy udpClient = null;
   private UDPClient udpClient = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this.getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_udp_client, container, false);
        init();
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (udpClient != null)
            udpClient.disConnectSocket();
    }

    private void init() {
        log("into init");
        Button btBroadcast = (Button)mView.findViewById(R.id.btBroadcast);
        btBroadcast.setOnClickListener(this);
        btSend = (Button) mView.findViewById(R.id.btSend);
        btSend.setOnClickListener(this);
        stConnect = (Switch) mView.findViewById(R.id.stConnect);
        //stConnect.setOnClickListener(this);
        //Switch swListen = (Switch) view.findViewById(R.id.swListen);
        stConnect.setOnCheckedChangeListener(checkedChange);

        etSendContent = (EditText) mView.findViewById(R.id.etSendContent);
        isHexS = (CheckBox) mView.findViewById(R.id.cbHexS);
        isHexS.setOnClickListener(this);
        isHexR = (CheckBox) mView.findViewById(R.id.cbHexR);
        isHexR.setOnClickListener(this);
        Button btClear = (Button) mView.findViewById(R.id.btClear);
        btClear.setOnClickListener(this);
        cbCyclicSend = (CheckBox) mView.findViewById(R.id.cbCyclicSend);
        cbCyclicSend.setOnClickListener(this);
        etCyclicTime=(EditText)mView.findViewById(R.id.etCyclicTime);
        sendInTime.setSendInTimeInterface(this);

    }

    CompoundButton.OnCheckedChangeListener checkedChange = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                log("开始连接。");
                initUdp();
                udpClient.connectSocket();
                etIP.setEnabled(false);
                //etLPort.setEnabled(false);
                etRPort.setEnabled(false);
            } else {
                log("断开连接。");
                udpClient.disConnectSocket();
                etIP.setEnabled(true);
                //etLPort.setEnabled(true);
                etRPort.setEnabled(true);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btSend:
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
                sendContent();
                break;
            case R.id.cbHexS:
                if (isHexS.isChecked()) {
                    udpClient.setSHex(true);
                } else {
                    udpClient.setSHex(false);
                }
                break;
            case R.id.btBroadcast:
                udpSendBroadcast(etSendContent.getText().toString());
                break;
            case R.id.cbHexR:
                if (udpClient != null) {
                    if (isHexR.isChecked()) {
                        udpClient.setRHex(true);
                    } else {
                        udpClient.setRHex(false);
                    }
                }
                break;
            case R.id.btClear:
                if (udpClient!=null){
                    udpClient.clearRecWindow();
                }else {
                    RDataWin.setText("");
                }
                break;
            case R.id.cbCyclicSend:
                if (udpClient != null) {
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
            default:
                break;
        }
    }

    private void initUdp() {
        etIP = (EditText) mView.findViewById(R.id.etIP);
        etRPort = (EditText) mView.findViewById(R.id.etRPort);
        //etLPort = (EditText) mView.findViewById(R.id.etLPort);
        //udpClient = new UDPServerCopy(mContext);
        udpClient = new UDPClient(mContext);
        if (etIP.getText().toString().equals("")) {
            udpClient.setServerIP(etIP.getHint().toString());
        } else {
            udpClient.setServerIP(etIP.getText().toString());
        }
        int RPortNum, LPortNum;
        if (etRPort.getText().toString().equals("")) {//默认端口为hint的值
            RPortNum = Integer.parseInt(etRPort.getHint().toString());
            udpClient.setServerPort(RPortNum);
        } else {
            RPortNum = Integer.parseInt(etRPort.getText().toString());
            udpClient.setServerPort(RPortNum);
        }
/*        if (etLPort.getText().toString().equals("")) {
            LPortNum = Integer.parseInt(etLPort.getHint().toString());
            udpClient.setLocalPort(LPortNum);
        } else {
            LPortNum = Integer.parseInt(etLPort.getText().toString());
            udpClient.setLocalPort(LPortNum);
            // udpClient.setLocalPort(8010);
        }*/
        RDataWin = (TextView) mView.findViewById(R.id.tvGet);
        udpClient.setRDataWindow(RDataWin);
    }

    private void udpSend(String content) {
        if (udpClient != null) {
            udpClient.sendData(content);
        }
    }
    private void udpSendBroadcast(String content) {
        if (udpClient != null) {
            udpClient.sendBroadcast(content);
        }
    }
    private void log(String str) {
        Log.i("chenxi", str + "  @UDPClientFragment");
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
}
