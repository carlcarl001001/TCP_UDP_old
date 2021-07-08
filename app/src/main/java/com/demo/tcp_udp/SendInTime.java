package com.demo.tcp_udp;

import android.os.Handler;

import com.demo.tcp_udp.Interface.ISendInTime;

/**
 * Created by Administrator on 2016/10/21.
 */
public class SendInTime {
    ISendInTime iSendInTime;
    TimerRunnable timerRunnable;
    public void setSendInTimeInterface(ISendInTime iFace)
    {
        this.iSendInTime=iFace;
    }
    public void starSendInTime(int time) {
        timerRunnable = new TimerRunnable(time);
        timerHandler.postDelayed(timerRunnable, time);//每两秒执行一次runnable.
    }

    public void stopSendInTime() {
        if (timerRunnable != null)
            timerHandler.removeCallbacks(timerRunnable);
    }

    Handler timerHandler = new Handler();

    private class TimerRunnable implements Runnable {
        int time;

        public TimerRunnable(int time) {
            this.time = time;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情
            // udpSend(etSend.getText().toString());
            iSendInTime.sendDataInTime();
            timerHandler.postDelayed(this, time);
        }
    }

}
