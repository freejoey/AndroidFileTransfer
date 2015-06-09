package com;

import android.os.Environment;

import java.io.File;

public class Constants {
	public static int HEAD_SIZE = 64;
	public static int PORT = 4468;
	//询问server的UDP
	public static int UDP_PORT_SEND = 4469;
	public static int UDP_PORT_RECV = 4470;
	public static int UDP_CONN_TIMEOUT = 1000;//链接超时
	public static String UDP_RSP = "_OK";

	public static String ROOT_PATH = Environment.getExternalStorageDirectory().getPath();
	public static String MY_APP_ROOT = ROOT_PATH + "/filetransfer";
	public static String RECV_FILE_PATH = MY_APP_ROOT + "/recv";
	public static String getAppPath(){
		File rcvDic = null;
		if (Tools.isSDExist()) {
			return RECV_FILE_PATH;
		}
		else {
			return System.getenv("SECONDARY_STORAGE") + "/filetransfer" + "/recv";
		}
	}
	//broadcast
	public static String NET_BROADCAST_FILTER = "com.ui.NetBroadcastReceiver";
	public static final int FLAG_RECV_FILE = 0;
	public static final int FLAG_REFUSE_RECV = 1;
	//发送数据类型
	public static final int NET_TYEP_SIZE = 16;
	public static final int NET_TYEP_RCV_RSP = 0;
	public static final int NET_TYEP_FILE_HEADER = 1;
}
