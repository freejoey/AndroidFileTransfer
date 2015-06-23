package com.ui;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.Constants;
import com.MyApplication;
import com.Tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by WangKui on 2015/6/7.
 */
public class SearchPointRunnable implements Runnable {
    private static final String Tag = "SearchPointRunnable";
    private String addr;
    private Context context;

    public SearchPointRunnable(Context context){
        this.context = context;
    }

    public SearchPointRunnable(String addr, Context context){
        this.addr = addr;
        this.context = context;
    }

    interface ConnListener{
        public void onAccessable(String addr);
        public void onUnaccess();
    }
    ConnListener listener;
    public void setListener(ConnListener listener){
        this.listener = listener;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new  DatagramSocket (Constants.UDP_PORT_SEND);
            String str = Tools.getMyAddr(context);
            //发送询问信息,格式：|ip地址|用户昵称|类型|
            str = "|" + str + "|" + MyApplication.getMyName() + "|" + String.valueOf(Constants.UDP_FIRST_REQ) + "|";
            byte data[] = str.getBytes();
            DatagramPacket p = new DatagramPacket (data , data.length , null , Constants.UDP_PORT_RECV);

            String head = "192.168.";
            String s1,s2;
            for (int i=0; i<3; i++){
                s1 = head + String.valueOf(i) + ".";
                for (int j=1; j<255; j++){
                    s2 = s1 + String.valueOf(j);
                    if (!s2.equals(Tools.getMyAddr(context))) {
                        InetAddress serverAddress = InetAddress.getByName(s2);
                        p.setAddress(serverAddress);
                        socket.send(p);
                        Log.i(Tag, "send udp to:" + s2);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (socket!=null) {
                socket.close();
                socket = null;
            }
        }
    }
}
