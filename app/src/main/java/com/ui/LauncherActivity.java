package com.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.Constants;
import com.MyApplication;
import com.Tools;
import com.aidl.NETInterface;
import com.aidl.NETService;
import com.example.myfiletransfer.R;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonRectangle;
import com.ui.MyFragmentDialog.OnItemClicked;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class LauncherActivity extends BaseFragmentActivity {
	private final String Tag = "LauncherActivity";
	private Context mContext;
	private LayoutInflater inflater;
	private ConnAddrAdapter adapter;
	public static Handler mHandler = null;

	private NETInterface netService = null;

	private ButtonRectangle btnFile;
	private TextView tvMyAddr;
	private ListView lvConn;

	private List<String> addrList = new ArrayList<String>();
	private List<String> states = new ArrayList<String>();
	private boolean isRefreshConn = false;
	String[] items = null;

	public static final int MSG_NEW_CONN = 1;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NEW_CONN:
				refreshConn();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle saveInstance) {
		super.onCreate(saveInstance);
		setContentView(R.layout.activity_launcher);
		mContext = this;
		inflater = getLayoutInflater();

		bindMyService();
		isRefreshConn = false;
		items = new String[] { "发送文件", "断开链接" };

		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initData();
		refreshConn();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler = null;
		unbindService(conn);
		// stopService(new Intent(this, ListenConnService.class));
		Log.i(Tag, "LauncherActivity onDestroy");
		stopServerListener();
	}

	private void initView() {
		lvConn = (ListView) findViewById(R.id.lv_connected_addr);
		adapter = new ConnAddrAdapter(inflater);
		lvConn.setAdapter(adapter);
		lvConn.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (states.get(position).equals("已链接")) {
					String addr = addrList.get(position);
					openSelector(addr);
				}
			}
		});

		tvMyAddr = (TextView) findViewById(R.id.tv_my_addr);
		btnFile = (ButtonRectangle) findViewById(R.id.btn_send_recv);
		btnFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(LauncherActivity.this, MainActivity.class);
				startActivity(i);
			}
		});
	}

	private void initData() {
		if (mHandler == null)
			mHandler = handler;

		String ip = Tools.getMyAddr(mContext);
		if (ip == null)
			tvMyAddr.setText("Wifi未开启");
		else
			tvMyAddr.setText(ip);

		File rcvDic = new File(Constants.getAppPath());
		if (!rcvDic.exists()) {
			try {
				boolean re = rcvDic.mkdirs();
				Log.i(Tag, "创建目录：" + rcvDic.getPath() + ", --->" + re);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void refreshConn() {
		Log.i(Tag, "refreshConn addr:");
		if (netService != null) {
			isRefreshConn = true;
			try {
				addrList.clear();
				states.clear();
				String[] addrs = netService.getAllConn();

				if (addrs != null) {
					int i = 0;
					while (i < addrs.length) {
						// 检测是否链接状态
						if (netService.isAddrConnected(addrs[i])) {
							states.add("已链接");
						} else {
							states.add("已断开");
						}

						addrList.add(addrs[i]);
						Log.i(Tag, addrs[i]);
						i++;
					}
					adapter.setData(addrList, states);
					adapter.notifyDataSetChanged();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initServerListener() {
		// startService(new Intent(this, ListenConnService.class));
		if (netService != null)
			try {
				netService.startListener();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void stopServerListener() {
		// startService(new Intent(this, ListenConnService.class));
		if (netService != null)
			try {
				netService.stopListener();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void openSelector(final String addr) {
		final MyFragmentDialog d = new MyFragmentDialog(mContext, items);
		d.setListener(new OnItemClicked() {

			@Override
			public void click(int i) {
				// TODO Auto-generated method stub
				if (i == 0) {
					Bundle b = new Bundle();
					b.putString(SendingActivity.ADDR_KEY, addr);
					Intent iSend = new Intent(LauncherActivity.this,
							SendingActivity.class);
					iSend.putExtras(b);
					startActivity(iSend);
				} else if (i == 1) {
					if (netService != null) {
						try {
							netService.disconnect(addr);
							refreshConn();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				d.dismiss();
			}
		});
		d.show(getSupportFragmentManager(), "MyFragmentDialog");
	}

	private void bindMyService() {
		// Intent intent=new Intent("com.dongzi.IStockQuoteService");
		Intent intent = new Intent(mContext, NETService.class);
		// startService(intent);//�����Ҫstopservice��ֹͣ

		bindService(intent, conn, mContext.BIND_AUTO_CREATE);

		// �鿴������service
		// ActivityManager mActivityManager = (ActivityManager)
		// getSystemService(Context.ACTIVITY_SERVICE);
		// List<ActivityManager.RunningServiceInfo> services =
		// mActivityManager.getRunningServices(20);
		// for(ActivityManager.RunningServiceInfo info: services){
		// // ��ø�Service�������Ϣ ������pkgname/servicename
		// ComponentName serviceCMP = info.service;
		// String serviceName = serviceCMP.getShortClassName(); // service ������
		// String pkgName = serviceCMP.getPackageName(); // ����
		// Log.i(Tag, "����:" + info.process + ", service:" + serviceName);
		// }
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			netService = (NETInterface) NETInterface.Stub.asInterface(service);
			MyApplication.setNetService(netService);
			if (!isRefreshConn)
				refreshConn();
			initServerListener();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
		}
	};

	// private void connect() {
	// if (netService == null)
	// return;
	//
	// // if(MySocketFactory.getInstance().isSocketExist(addr))
	// // {
	// // tvState.setText("�Ѿ����ӵ�:" + addr);
	// // return;
	// // }
	//
	// tvState.setText("����������...");
	//
	// addr = etAddr.getText().toString();
	// if (addr != null && !addr.equals("")) {
	// new Thread() {
	// public void run() {
	// try {
	// int re = netService.connect(addr);
	// Message msg = new Message();
	// msg.what = MSG_CONNECTED;
	// msg.obj = re;
	// handler.sendMessage(msg);
	// } catch (RemoteException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }.start();
	// ;
	// }
	// }

	class ConnAddrAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<String> addrList = new ArrayList<String>();
		private List<String> states = new ArrayList<String>();

		public ConnAddrAdapter(LayoutInflater inflater) {
			this.inflater = inflater;
		}

		public void setData(List<String> data, List<String> s) {
			this.addrList = data;
			this.states = s;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return addrList == null ? 0 : addrList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return addrList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_addr_list, null);
				holder = new ViewHolder();
				holder.tvAddr = (TextView) convertView
						.findViewById(R.id.tv_addr);
				holder.tvState = (TextView) convertView
						.findViewById(R.id.tv_state);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tvAddr.setText(addrList.get(position));
			holder.tvState.setText(states.get(position));

			return convertView;
		}

	}

	public final class ViewHolder {
		public TextView tvAddr;
		public TextView tvState;
	}
}
