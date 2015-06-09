package com.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.myfiletransfer.R;

import java.lang.String;
import java.lang.System;
import java.util.ArrayList;

public class NetBroadcastReceiver extends BroadcastReceiver {
	private final String Tag = "NetBroadcastReceiver";
	private Context mContext;
	public static final int FLAG_NEW_CONN = 1;
	public static final int FLAG_NEW_RCV_FILE = 2;
	public static final int FLAG_LOSE_CONN = 3;
	public static final int FLAG_IS_RECV = 4;
	public static final int FLAG_TARGET_REFUSE = 5;
	public static final int FLAG_SEND_OK = 6;
	public static final int FLAG_SEND_ERROR = 7;
	public static final int FLAG_UDP_NEW_POINT = 8;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub\
		this.mContext = context;
		String action = intent.getAction();
		int type = intent.getIntExtra("type", 0);

		// �����ӵ���,֪ͨLauncherActivity���½���
		if (type == FLAG_NEW_CONN) {
			if (LauncherActivity.mHandler != null) {
				Message msg = new Message();
				msg.what = LauncherActivity.MSG_NEW_CONN;
				LauncherActivity.mHandler.sendMessage(msg);
			}
		} else if (type == FLAG_NEW_RCV_FILE) {
			if (RecvFileFragment.mHandler != null) {
				Log.i(Tag, "收到广播FLAG_NEW_RCV_FILE");
				Message msg = new Message();
				msg.what = RecvFileFragment.MSG_NEW_RCV_FILE;
				RecvFileFragment.mHandler.sendMessage(msg);
			}
		}
		// ֪ͨLauncherActivity���½���
		else if (type == FLAG_IS_RECV) {
			Log.i(Tag, "收到广播FLAG_LOSE_CONN");
			String fileName = intent.getStringExtra("file");
			String addr = intent.getStringExtra("addr");
			Intent i = new Intent(context, IsRecvDialogActivity.class);
			i.putExtra("addr", addr);
			i.putExtra("file", fileName);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
		// ֪ͨ�Է��ܾ�����
		else if(type == FLAG_TARGET_REFUSE){
			Log.i(Tag, "收到广播FLAG_TARGET_REFUSE");
			if (SendingActivity.mHandler != null) {
				String fileName = intent.getStringExtra("file");
				String addr = intent.getStringExtra("addr");
				
				Bundle b = new Bundle();
				b.putString("file", fileName);
				b.putString("addr", addr);
				Message msg = new Message();
				msg.what = SendingActivity.MSG_TARGET_REFUSE;
				msg.setData(b);
				SendingActivity.mHandler.sendMessage(msg);
			}
		}
		else if(type == FLAG_SEND_OK){
			Log.i(Tag, "收到广播FLAG_SEND_OK");
			if (SendingActivity.mHandler != null) {
				String fileName = intent.getStringExtra("file");
				String addr = intent.getStringExtra("addr");

				if(SendingActivity.mHandler!=null) {
					Bundle b = new Bundle();
					b.putString("file", fileName);
					b.putString("addr", addr);
					Message msg = new Message();
					msg.what = SendingActivity.MSG_SEND_FIN;
					msg.setData(b);
					SendingActivity.mHandler.sendMessage(msg);
				}
				else{
					String s = "接收完成:"+fileName;
					showNotice(s, 0);
				}
			}
		}
		else if(type == FLAG_SEND_ERROR){
			Log.i(Tag, "收到广播FLAG_SEND_ERROR");
			if (SendingActivity.mHandler != null) {
				String fileName = intent.getStringExtra("file");
				String addr = intent.getStringExtra("addr");
				int errCode = intent.getIntExtra("errCode", 0);

				if(SendingActivity.mHandler!=null) {
					Bundle b = new Bundle();
					b.putString("file", fileName);
					b.putString("addr", addr);
					b.putInt("errCode", errCode);
					Message msg = new Message();
					msg.what = SendingActivity.MSG_SEND_ERROR;
					msg.setData(b);
					SendingActivity.mHandler.sendMessage(msg);
				}
				else{
					String s = "接收文件出错:"+fileName;
					showNotice(s, 0);
				}
			}
		}
		//有新的可连接点
		else if(type == FLAG_UDP_NEW_POINT){
			ArrayList<String> addrs = intent.getStringArrayListExtra("addrs");
			if(addrs!=null)
				for (int i=0; i<addrs.size(); i++){
					Log.i(Tag, "可连接点:" + addrs.get(i));
				}
		}

	}

	private void showNotice(String content, int type){
		NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.ic_launcher, mContext.getResources().getString(R.string.app_name), System.currentTimeMillis());
		n.flags = Notification.FLAG_AUTO_CANCEL;
		Intent i = new Intent(mContext, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(
				mContext,
				R.string.app_name,
				i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		n.setLatestEventInfo(
				mContext,
				mContext.getResources().getString(R.string.app_name),
				content,
				contentIntent
		);
		nm.notify(R.string.app_name, n);
	}

}
