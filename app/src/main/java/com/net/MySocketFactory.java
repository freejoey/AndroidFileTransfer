package com.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;

public class MySocketFactory {
	private static final String Tag = "MySocketFactory";
	private static final MySocketFactory instance = new MySocketFactory();

	private static HashMap<String, ClientThread> connPool = new HashMap<String, ClientThread>();

	public MySocketFactory() {
		//connPool = new HashMap<String, ClientThread>();
	}

	public static MySocketFactory getInstance() {
//		if (instance == null) {
//			Log.i(Tag, "新建MySocketFactory");
//			instance = new MySocketFactory();
//		} else
//			Log.i(Tag, "获取MySocketFactory");

		return instance;
	}

	public int getPoolSize() {
		if (connPool == null)
			return 0;
		else {
			return connPool.size();
		}
	}

	public HashMap<String, ClientThread> getAllConn() {
		return connPool;
	}

	public boolean isSocketExist(String addr) {
		return connPool.containsKey(addr);
	}

	public void addSocketConn(String key, ClientThread s) {
		Log.i(Tag, "添加一个socket中。。。 -》" + key);
		if (connPool != null) {
			connPool.put(key, s);
			s.start();
			Log.i(Tag, "检测添加是否成功 -》" + connPool.get(key));
		}
	}

	public ClientThread getThreadByAddr(String addr) {
		ClientThread re = connPool.get(addr);
		return re;
	}

	public boolean isAddrConnected(String addr) {
		ClientThread t = connPool.get(addr);
		return t.isServerConnected();
	}

	public void removeSocketConn(String addr) {
		if (connPool != null) {
			ClientThread t = connPool.get(addr);
			if (t != null) {
				if (t.isRunning())
					t.stopThread();
				connPool.remove(addr);
			}
		}
	}

	public void clearPool() {
		if (connPool != null) {
			// Map map = new HashMap();
			Iterator iter = connPool.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				ClientThread c = (ClientThread) entry.getValue();
				if (c != null && c.isRunning())
					c.stopThread();
			}
			connPool.clear();
			Log.i(Tag, "销毁所有链接线程.");
		}
	}
}
