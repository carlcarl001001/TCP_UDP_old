package com.demo.tcp_udp;

import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Created by chenxi on 2016/10/13.
 */
public class DataFormat {
    private static DataFormat dataFormat=new DataFormat();
    public static DataFormat getInstance(){
        return dataFormat;
    }
    public byte[] strToHex(String SData) throws Exception
    {
        byte[] sBuffer;
        String[] dateStr = SData.split(" ");
        sBuffer = new byte[dateStr.length];
        for (int i = 0; i < dateStr.length; i++) {
            // try {
            sBuffer[i] = StrToByte(String.valueOf(dateStr[i]));
    /*        } catch (Exception e) {
                loge("line:" + getLineNumber(new Exception())+"erro:" + e.getMessage());
            }*/
        }
        return sBuffer;
    }

    private byte StrToByte(String s) {
        return Integer.valueOf(String.valueOf(Integer.parseInt(s, 16))).byteValue();
    }
    public String byteToStr(byte[] datas)
    {
        String str="";
        if (datas == null || datas.length <= 0) {
            return null;
        }
        for (byte data:datas)
        {
            String dataStr = Integer.toHexString((data&0xff));
            if (dataStr.length() < 2) {
                dataStr="0"+dataStr;
            }
            str=str+" "+dataStr;
        }
        return str;
    }
    public String hexToStr(byte[] rBuffer,int len) {
        byte[] fBuffer = new byte[len];
        fBuffer = new byte[len];
        System.arraycopy(rBuffer, 0, fBuffer, 0, len);
        StringBuilder sb = new StringBuilder(fBuffer.length);
        String rHex = "";
        for (byte aFBuffer : fBuffer) {
            rHex = Integer.toHexString(aFBuffer & 0xFF);
            if (rHex.length() == 1)
                rHex = "0" + rHex;
            sb.append((rHex.toUpperCase() + " "));
        }
        return sb.toString().trim();
    }
    public boolean CharInRange(char c) {
        boolean result = false;
        if (c >= '0' && c <= '9')
            result = true;
        if (c >= 'a' && c <= 'f')
            result = true;
        if (c >= 'A' && c <= 'F')
            result = true;
        return result;
    }
    public String char2Str(char[] buff, int count) {
        char[] temp = new char[count];
        for (int i = 0; i < count; i++) {
            temp[i] = buff[i];
        }
        return new String(temp);
    }
    // char转byte

    public byte[] char2Bytes (char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put (chars);
        cb.flip ();
        ByteBuffer bb = cs.encode (cb);
        return bb.array();

    }

// byte转char

    public char[] byte2Char (byte[] bytes) {
        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes);
        bb.flip ();
        CharBuffer cb = cs.decode(bb);

        return cb.array();
    }

    private void log(String str) {
        Log.i("chenxi", str + "  @DataFormat");
    }
    private void loge(String str) {
        Log.e("chenxi", str + "  @DataFormat");
    }

}
