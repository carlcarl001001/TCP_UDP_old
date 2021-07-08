package com.demo.tcp_udp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.demo.tcp_udp.fragment.TCPClientFragment;
import com.demo.tcp_udp.fragment.TCPServerFragment;
import com.demo.tcp_udp.fragment.UDPClientFragment;
import com.demo.tcp_udp.fragment.UDPServerFragment;

import cn.waps.AppConnect;


public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private FrameLayout radiolayout;
    private int windowWidth;
    private FragmentManager fragmentManager;
    private Fragment lastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化统计器，并通过代码设置APP_ID, APP_PID
        //AppConnect.getInstance("decfa832f6965a2928cb3b833d5b7851", "QQ", this);
        // 互动广告调用方式
        LinearLayout layout = (LinearLayout) this.findViewById(R.id.AdLinearLayout);
        AppConnect.getInstance(this).showBannerAd(this, layout);

        getInfo();
        setTitle(R.string.TCPServer);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        fragmentManager = getSupportFragmentManager();
        //TCPServerFragment tcpServerFragment = (TCPServerFragment) fragmentManager.findFragmentById(R.id.content_fragment);
        //TCPClientFragment tcpClientFragment = (TCPClientFragment) fragmentManager.findFragmentById(R.id.content_fragment);
        //UDPServerFragment udpServerFragment =(UDPServerFragment) fragmentManager.findFragmentById(R.id.content_fragment);
        //UDPClientFragment udpClientFragment =(UDPClientFragment) fragmentManager.findFragmentById(R.id.content_fragment);
        TCPServerFragment tcpServerFragment = new TCPServerFragment();
        fragmentManager.beginTransaction().replace(R.id.content_fragment, tcpServerFragment).commit();
    }

    @Override
    protected void onDestroy() {
        AppConnect.getInstance(this).close();
        super.onDestroy();
    }

    private void getInfo()//获取手机的分辨率信息
    {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        windowWidth = metric.widthPixels; // 屏幕宽度（像素）
        int height = metric.heightPixels; // 屏幕高度（像素）
        float density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        log("分辨率：" + windowWidth + "x" + height);
        log("densityDpi:" + densityDpi + " density:" + density);
    }

    private void log(String str) {
        Log.i("chenxi", str + "  @MainActivity");
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.rb_tcpserver:
                setTitle(R.string.TCPServer);
                TCPServerFragment tcpServerFragment = new TCPServerFragment();
                fragmentManager.beginTransaction().replace(R.id.content_fragment, tcpServerFragment).commit();
                lastFragment = tcpServerFragment;


                // checkCodeFragment = new CheckCodeFragment();
                //transaction.add(R.id.activity_regist, checkCodeFragment).commit();
                break;
            case R.id.rb_tcpclient:
                //MoveCover.moveCoverView(img, startLeft, img.getWidth(), 0, 0);
                //startLeft = img.getWidth();
                setTitle(R.string.TCPClient);
                TCPClientFragment tcpClientFragment = new TCPClientFragment();
                fragmentManager.beginTransaction().replace(R.id.content_fragment, tcpClientFragment).commit();
                lastFragment = tcpClientFragment;

                break;
            case R.id.rb_udpserver:
               // MoveCover.moveCoverView(img, startLeft, img.getWidth() * 2, 0, 0);
                setTitle(R.string.UDPServer);
                UDPServerFragment udpServerFragment = new UDPServerFragment();
                fragmentManager.beginTransaction().replace(R.id.content_fragment, udpServerFragment).commit();
                break;
            case R.id.rb_udpclient:
                setTitle(R.string.UDPClient);
                UDPClientFragment udpClientFragment = new UDPClientFragment();
                fragmentManager.beginTransaction().replace(R.id.content_fragment, udpClientFragment).commit();
                break;
            default:
                break;
        }
    }

}
