package com.aidl;

interface NETInterface{
	int connect(String addr);
	int sendFile(String addr, String filePath);
	int isRecvFile(String remoteAddr, String fileName, boolean isRcv);
	String[] getAllConn();
	boolean disconnect(String addr);
	boolean startListener();
	boolean stopListener();
	boolean isAddrConnected(String addr);
}