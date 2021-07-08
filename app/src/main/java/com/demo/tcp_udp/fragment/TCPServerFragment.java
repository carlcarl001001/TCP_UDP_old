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
import android.widget.AdapterView;
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
import com.demo.tcp_udp.utils.CommonUtils;

/**
 * Created by Administrator on 2016/10/3.
 */
public class TCPServerFragment extends Fragment implements View.OnClickListener,ISendInTime{
    private TCPServer tcpServer;
    private View mView;
    private String IP = "";
    private String getContent="";
    private EditText etSendContent;
    private CheckBox cbHexR;
    private CheckBox cbHexS;
    //private MyHander mhander = new MyHander();
    private TextView tvGet;
    private TextView RDataWin;
    private Spinner spClientsIp;
    private Context mContext;
    private CheckBox cbCyclicSend;
    private EditText etCyclicTime;
    private EditText etPort;
    private int time;
    private SendInTime sendInTime=new SendInTime();
    //监听端口号
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_tcp_server, container, false);
        init();
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tcpServer!=null) {
            tcpServer.disconnectSocket();
        }
    }

    private void init() {
        RDataWin = (TextView) mView.findViewById(R.id.tvGet);
        spClientsIp =(Spinner)mView.findViewById(R.id.spClientsIp);
        //spClientsIp.setOnItemSelectedListener(this);
        TextView tvIp = (TextView) mView.findViewById(R.id.tvIp);
        etPort = (EditText) mView.findViewById(R.id.etPort);
        etSendContent=(EditText)mView.findViewById(R.id.etSendContent);
        tvGet= (TextView) mView.findViewById(R.id.tvGet);
        cbHexR=(CheckBox)mView.findViewById(R.id.cbHexR);
        cbHexR.setOnClickListener(this);
        cbHexS=(CheckBox)mView.findViewById(R.id.cbHexS);
        cbHexS.setOnClickListener(this);
        Button btSend=(Button)mView.findViewById(R.id.btSend);
        btSend.setOnClickListener(this);
        Button btClear=(Button)mView.findViewById(R.id.btClear);
        btClear.setOnClickListener(this);
        IP = CommonUtils.getInstance().getLocalIP();
        tvIp.setText(IP);
        Switch swListen = (Switch) mView.findViewById(R.id.swListen);
        swListen.setOnCheckedChangeListener(clickListen);
        cbCyclicSend=(CheckBox)mView.findViewById(R.id.cbCyclicSend);
        cbCyclicSend.setOnClickListener(this);
        etCyclicTime=(EditText)mView.findViewById(R.id.etCyclicTime);
        sendInTime.setSendInTimeInterface(this);
        log("IP:" + IP + " defPort:" + getResources().getString(R.string.defPort));
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
                //int port= Integer.valueOf(getResources().getString(R.string.defPort));
                tcpServer = new TCPServer(mContext,port);
                tcpServer.setRDataWindow(RDataWin);
                tcpServer.setCleintsIpSpinner(spClientsIp);
                tcpServer.connectSocket();
                etPort.setEnabled(false);

            } else {
                tcpServer.disconnectSocket();
                tcpServer=null;
                etPort.setEnabled(true);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btSend:
                InputMethodManager imm=(InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                sendContent();
                break;
            case R.id.cbHexR:
                log("cbHexS.isChecked():" + cbHexR.isChecked());
                if (tcpServer != null) {
                    if (cbHexR.isChecked()) {
                        tcpServer.setIsRHex(true);
                    } else {
                        tcpServer.setIsRHex(false);
                    }
                }
                break;
            case R.id.cbHexS:
                log("cbHexR.isChecked():" + cbHexS.isChecked());
                if (tcpServer != null) {
                    if (cbHexS.isChecked()) {
                        tcpServer.setIsSHex(true);
                    } else {
                        tcpServer.setIsSHex(false);
                    }
                }
                break;
            case R.id.cbCyclicSend:
                if (tcpServer != null) {
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
                if (tcpServer!=null){
                    tcpServer.clearRecWindow();
                }else {
                    RDataWin.setText("");
                }
                break;
            default:
                break;
        }
    }
    private void tcpSend(String content){
        if (tcpServer!=null)
            tcpServer.sendData(content);
    }
    private void log(String str) {
        Log.i("chenxi", str + "  @MainActivity");
    }

    @Override
    public void sendDataInTime() {
        sendContent();
    }
    private void sendContent(){
        if (!"".equals(etSendContent.getText().toString())){
            tcpSend(etSendContent.getText().toString());
        }else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.send_content_can_not_null), Toast.LENGTH_LONG).show();
        }
    }
}
