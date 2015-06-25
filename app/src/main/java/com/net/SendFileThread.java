package com.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.Constants;
import com.Tools;
import com.ui.NetBroadcastReceiver;

public class SendFileThread extends Thread {
	private final String Tag = "SendFileThread";
	private Context mContext;

	private FileInputStream fis = null;
	private BufferedInputStream bfis = null;
	private BufferedOutputStream outSocket;
	Socket socket;
	String filePath;
	private boolean agreeRecv = false;

	public SendFileThread(Socket socket, String filePath, Context context) throws IOException {
		this.mContext = context;
		this.socket = socket;
		this.filePath = filePath;
		this.outSocket = new BufferedOutputStream(this.socket.getOutputStream());
	}

	public void setAgreeAble(boolean b){
		agreeRecv = b;
	}
	
	public void sendType(int type) {
		if (outSocket != null) {
			try {
				outSocket.write(type);
				outSocket.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int sendHeader(String s) {
		int re = -1;
		byte[] bData = s.getBytes();
		byte[] sendData = new byte[Constants.HEAD_SIZE];
		for (int i = 0; i < sendData.length; i++) {
			if (i < bData.length) {
				sendData[i] = bData[i];
			} else
				sendData[i] = (byte) (i);
		}

		if (outSocket != null) {
			try {
				outSocket.write(sendData);
				outSocket.flush();
				re = 0;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return re;
	}

	public void send(int b) throws IOException {
		if (outSocket != null) {
			outSocket.write(b);
		}
	}

	public void run() {
		File file = new File(filePath);
		if (file.isDirectory()) {
			// 文件夹不能发送
			listener.onReturn(-2);
			return;
		}
		try {
			agreeRecv = false;
			// 发送数据标示
			sendType(Constants.NET_TYEP_FILE_HEADER);

			// 发送头部
			// long size = Tools.getFileSize(file);
			long size = file.length();
			String fName = Tools.getFileName(filePath);
			String header = "|" + fName + "|" + String.valueOf(size) + "|";
			if (sendHeader(header) == -1) {
				listener.onReturn(-1);
				return;
			}

			synchronized (this) {
				this.wait();
			}
			Log.i("aaaaaaa", "对方是否同意接收" + agreeRecv);

			if (agreeRecv) {
				// 发送文件内容
				fis = new FileInputStream(file);
				bfis = new BufferedInputStream(fis);
				int t;
				long absSent = 0;
				int rCount = 0; //进度条记录(精度 1%)
				int stepCount = (int) size /100;
				int progress = 0;
				while ((t = bfis.read()) != -1) {
					send(t);
					absSent++;

					rCount++;
					if (rCount >= stepCount){
						rCount = 0;
						progress++;
						Intent intent = new Intent(
								Constants.NET_BROADCAST_FILTER);
						intent.putExtra("type",
								NetBroadcastReceiver.FLAG_SENDING_PROGRESS);
						intent.putExtra("progress", progress);
						intent.putExtra("fileName", fName);
						mContext.sendBroadcast(intent);// 传递过去
					}
				}
				//确认发送结束
				Intent intent = new Intent(
						Constants.NET_BROADCAST_FILTER);
				intent.putExtra("type",
						NetBroadcastReceiver.FLAG_SENDING_PROGRESS);
				intent.putExtra("progress", 100);
				intent.putExtra("fileName", fName);
				mContext.sendBroadcast(intent);// 传递过去

				outSocket.flush();
				Log.i(Tag, "发送文件实际长度:" + absSent + ", 预读文件长度:" + size);
				listener.onFinished();
			}
			else{
				listener.onRefuse();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			listener.onReturn(-3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			listener.onReturn(-1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
				if (bfis != null) {
					bfis.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	OnThreadListener listener;

	public void setListener(OnThreadListener listener) {
		this.listener = listener;
	}

	public interface OnThreadListener {
		public void onReturn(int errCode);
		public void onRefuse();
		public void onFinished();
	}
}
