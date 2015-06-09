package com.aidl;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.Constants;
import com.net.ClientThread;
import com.net.ListenConnService;
import com.net.MySocketFactory;
import com.net.ServerThread;
import com.net.ServerThread.ListenServerState;
import com.net.UdpServerThread;
import com.ui.NetBroadcastReceiver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class NETService extends Service {
	private final String Tag = "NETService";
	private ServerThread server = new ServerThread(this);
	private UdpServerThread udpServer = new UdpServerThread(this);

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		Log.i(Tag, "NETService onDestroy");
		MySocketFactory.getInstance().clearPool();
	}

	private NETInterface.Stub mBinder = new NETInterface.Stub() {
		private int re = -1;

		@Override
		public int sendFile(String addr, String filePath)
				throws RemoteException {
			// TODO Auto-generated method stub
			re = -1;
			if (MySocketFactory.getInstance().getThreadByAddr(addr) != null) {
				re = MySocketFactory.getInstance().getThreadByAddr(addr)
						.sendFile(filePath);
			} else {
				re = -2;
				Log.i(Tag, "找不到对应socket");
			}
			return re;
		}

		@Override
		public int isRecvFile(String remoteAddr, String fileName, boolean isRecv)
				throws RemoteException {
			// TODO Auto-generated method stub
			re = -1;
			if (MySocketFactory.getInstance().getThreadByAddr(remoteAddr) != null) {
				MySocketFactory.getInstance().getThreadByAddr(remoteAddr).notifyRecv(isRecv);;
				re = 0;
			} else {
				re = -2;
				Log.i(Tag, "找不到对应socket");
			}
			return 0;
		}

		@Override
		public int connect(final String addr) throws RemoteException {
			// TODO Auto-generated method stub
			re = -1;

			try {
				Socket s = new Socket(addr, Constants.PORT);
				if (s != null) {
					ClientThread ct = new ClientThread(s, NETService.this);
					MySocketFactory.getInstance().addSocketConn(addr, ct);
					re = 0;

					if (MySocketFactory.getInstance().getThreadByAddr(addr) == null) {
						Log.i(Tag, "添加socket失败。。。");
					} else {
						Log.i(Tag, "添加socket成功:"
								+ MySocketFactory.getInstance()
										.getThreadByAddr(addr));
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Log.i("NETService", "state:" + re);
			return re;
		}

		@Override
		public String[] getAllConn() throws RemoteException {
			// TODO Auto-generated method stub
			String re[] = null;

			HashMap<String, ClientThread> maps = MySocketFactory.getInstance()
					.getAllConn();
			if (maps != null)
				re = new String[maps.size()];
			Iterator iter = maps.entrySet().iterator();
			int i = 0;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String c = (String) entry.getKey();
				re[i] = c;
				i++;
			}

			return re;
		}

		@Override
		public boolean disconnect(String addr) throws RemoteException {
			// TODO Auto-generated method stub
			MySocketFactory.getInstance().removeSocketConn(addr);
			return true;
		}

		@Override
		public boolean startListener() throws RemoteException {
			// TODO Auto-generated method stub
			// startService(new Intent(NETService.this,
			// ListenConnService.class));
			if(udpServer==null) {
				udpServer = new UdpServerThread(NETService.this);
			}
			udpServer.start();

			if (server == null) {
				server = new ServerThread(NETService.this);
			}
			server.start();
			server.setServerListener(new ListenServerState() {

				@Override
				public void onReady() {
					// TODO Auto-generated method stub
					Log.i(Tag, "开启服务端service,  等待链接...");

					// Intent intent = new
					// Intent(Constants.NET_BROADCAST_FILTER);
					// intent.putExtra("Name", "hellogv");
					// intent.putExtra("Blog",
					// "http://blog.csdn.net/hellogv");
					// sendBroadcast(intent);//传递过去
				}

				@Override
				public void onConnected(String addr) {
					// TODO Auto-generated method stub
					Log.i(Tag, "接收到一个链接：" + addr);

					Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
					intent.putExtra("type", NetBroadcastReceiver.FLAG_NEW_CONN);
					sendBroadcast(intent);// 传递过去
				}
			});

			return true;
		}

		@Override
		public boolean stopListener() throws RemoteException {
			// TODO Auto-generated method stub
			// stopService(new Intent(NETService.this,
			// ListenConnService.class));
			if (server != null && server.isRunning()) {
				server.stopThread();
				server = null;
			}
			if(udpServer!=null){
				udpServer.stopThread();
				udpServer = null;
			}
			return true;
		}

		@Override
		public boolean isAddrConnected(String addr) throws RemoteException {
			// TODO Auto-generated method stub
			return MySocketFactory.getInstance().isAddrConnected(addr);
		}
	};
}
