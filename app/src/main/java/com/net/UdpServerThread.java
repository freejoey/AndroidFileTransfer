package com.net;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.Constants;
import com.MyApplication;
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
    private static final String  Tag = "UdpServerThread";
    private Context mContext;
    private DatagramSocket socket = null;
    private boolean isRun = true;
    private ArrayList<UserMode> accAddr = new ArrayList<UserMode>();

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
        byte dAddr[] = new byte[Constants.UDP_RCV_SIZE];
        DatagramPacket pAddr = new DatagramPacket(dAddr , dAddr.length);
        while(isRun) {
            try {
                socket.receive(pAddr);
                String sAddr = new String(pAddr.getData() , pAddr.getOffset() , pAddr.getLength());
                //拆分收到的信息,格式：|ip地址|用户昵称|类型|
                String sData[] =  sAddr.split("\\|");
                if (sData.length>3) {
                    String ipAddr = sData[1];
                    String name = sData[2];
                    String sType = sData[3];
                    Log.i(Tag, "全部信息:" + sAddr);
                    Log.i(Tag, "用户昵称:" + name);
                    boolean isExist = false;
                    for (int i=0; i<accAddr.size(); i++){
                        if(accAddr.get(i).getIp().equals(ipAddr)){
                            isExist = true;
                            break;
                        }
                    }
                    if(!isExist) {
                        UserMode user = new UserMode(ipAddr, name);
                        accAddr.add(user);
                        //广播通知有新节点
                        Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
                        intent.putExtra("type", NetBroadcastReceiver.FLAG_UDP_NEW_POINT);
                        //intent.putStringArrayListExtra("addrs", accAddr);
                        intent.putParcelableArrayListExtra("addrs", accAddr);
                        mContext.sendBroadcast(intent);// 传递到ui端
                    }
                    if(Integer.parseInt(sType) == Constants.UDP_FIRST_REQ){
                        //回复一条确认消息
                        InetAddress serverAddress = InetAddress.getByName(ipAddr);
                        //String str = Constants.UDP_RSP;
                        String str = Tools.getMyAddr(mContext);
                        str = "|" + str + "|" + MyApplication.getMyName() + "|" + String.valueOf(Constants.UDP_SCND_ACK) + "|";
                        byte data[] = str.getBytes();
                        DatagramPacket p = new DatagramPacket(data, data.length, serverAddress, Constants.UDP_PORT_RECV);
                        socket.send(p);
                    }
                    //socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
