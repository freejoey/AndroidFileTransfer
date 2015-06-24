package com;

import android.os.Environment;

import java.io.File;

public class Constants {
	public static int HEAD_SIZE = 64;
	public static int PORT = 4468;
	//询问server的UDP
	public static int UDP_PORT_SEND = 4469;
	public static int UDP_PORT_RECV = 4470;
	public static int UDP_RCV_SIZE = 64;
	//区分是回复，还是搜索UDP消息
	public static int UDP_FIRST_REQ = 0;//搜索节点时的询问标记
	public static int UDP_SCND_ACK = 1;//回复询问时

	public static String ROOT_PATH = Environment.getExternalStorageDirectory().getPath();
	public static String MY_APP_ROOT = ROOT_PATH + "/filetransfer";
	public static String RECV_FILE_PATH = MY_APP_ROOT + "/recv/";
	public static String CRASH_FILE_PATH = MY_APP_ROOT+ "/crash/";
	public static String getAppPath(){
		File rcvDic = null;
		if (Tools.isSDExist()) {
			return RECV_FILE_PATH;
		}
		else {
			return System.getenv("SECONDARY_STORAGE") + "/filetransfer" + "/recv/";
		}
	}
	public static String getCrashPath(){
		File rcvDic = null;
		if (Tools.isSDExist()) {
			return CRASH_FILE_PATH;
		}
		else {
			return System.getenv("SECONDARY_STORAGE") + "/filetransfer" + "/crash/";
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
