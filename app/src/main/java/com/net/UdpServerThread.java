package com.net;

import android.content.Context;
import android.content.Intent;

import com.Constants;
import com.Tools;
import com.ui.NetBroadcastReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by WangKui on 2015/6/8.
 */
public class UdpServerThread extends Thread {
    private Context mContext;
    private DatagramSocket socket = null;
    private boolean isRun = true;
    private ArrayList<String> accAddr = new ArrayList<String>();

    public UdpServerThread(Context context){
        this.mContext = context;
        try {
            socket = new  DatagramSocket (Constants.UDP_PORT_RECV);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void stopThread(){
        isRun = false;
        if(socket!=null){
            socket.close();
            socket = null;
        }
    }

    public void run(){
        byte dAddr[] = new byte[16];
        DatagramPacket pAddr = new DatagramPacket(dAddr , dAddr.length);
        while(isRun) {
            try {
                socket.receive(pAddr);
                String sAddr = new String(pAddr.getData() , pAddr.getOffset() , pAddr.getLength());
                if(!accAddr.contains(sAddr)) {
                    accAddr.add(sAddr);
                    //广播通知有新节点
                    Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
                    intent.putExtra("type", NetBroadcastReceiver.FLAG_UDP_NEW_POINT);
                    intent.putStringArrayListExtra("addrs", accAddr);
                    mContext.sendBroadcast(intent);// 传递过去

                    InetAddress serverAddress = InetAddress.getByName(sAddr);
                    //String str = Constants.UDP_RSP;
                    String str = Tools.getMyAddr(mContext);
                    byte data[] = str.getBytes();
                    DatagramPacket p = new DatagramPacket(data, data.length, serverAddress, Constants.UDP_PORT_RECV);
                    socket.send(p);
                    //socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
