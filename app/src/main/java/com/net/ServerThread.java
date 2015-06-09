package com.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.util.Log;

import com.Constants;

public class ServerThread extends Thread {
	private final String Tag = "ServerThread";
	private ServerSocket server = null;
	private Socket clientObject = null;
	private boolean isRun = false;

	private Context mContext;

	public ServerThread(Context mContext) {
		this.mContext = mContext;
	}

	public boolean isRunning() {
		return this.isRun;
	}

	public void stopThread() {
		this.interrupt();
		isRun = false;
		Log.i(Tag, "ServerThread服务端关闭");
		try {
			if (this.server != null)
				server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		isRun = true;

		try {
			server = new ServerSocket(Constants.PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (isRun) {
			if (server != null) {
				try {
					listener.onReady();
					clientObject = server.accept();
					if (clientObject != null) {
						ClientThread ct = new ClientThread(clientObject,
								mContext);
						MySocketFactory.getInstance().addSocketConn(
								clientObject.getInetAddress().getHostAddress(),
								ct);

						listener.onConnected(clientObject.getInetAddress()
								.getHostAddress());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (isRunning())
						stopThread();
				}
			}
		}
	}

	ListenServerState listener;

	public void setServerListener(ListenServerState listener) {
		this.listener = listener;
	}

	public interface ListenServerState {
		public void onReady();

		public void onConnected(String addr);
	}
}
