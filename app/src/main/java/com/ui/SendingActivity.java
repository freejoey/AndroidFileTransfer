package com.ui;

import java.util.ArrayList;

import com.Tools;
import com.aidl.NETInterface;
import com.aidl.NETService;
import com.example.myfiletransfer.R;
import com.net.UserMode;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SendingActivity extends Activity {
	private final String Tag = "SendingActivity";
	private Context mContext;
	private NETInterface netService = null;
	public static Handler mHandler;

	private EditText etAddr;
	private Button btSend;
	private TextView tvState, tvChose;
	private ListView lvAccPoints;
	private AccPointsAdapter accPointsAdapter;

	private String addr = null;

	public static final String ADDR_KEY = "_addr_key";
	public static final int MSG_CONNECTED = 0;
	public static final int MSG_TARGET_REFUSE = 1;
	public static final int MSG_SEND_FIN = 2;
//	public static final int MSG_SEND_ERROR = 3;
	public static final int MSG_POINT_AVAILABLE = 4;

	/*
	 * 当前状态: 0: 非链接 1：链接 2：正在发送 3：发送完成
	 */
	private int stat = 0;

	@Override
	protected void onCreate(Bundle saveInstance) {
		super.onCreate(saveInstance);
		setContentView(R.layout.activity_sending_file);
		mContext = this;
		mHandler = handler;

		addr = getIntent().getExtras().getString(ADDR_KEY);
		bindMyService();

		initView();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(mHandler==null)
			mHandler = handler;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
	}

	private void initView() {
		lvAccPoints = (ListView) findViewById(R.id.lv_sending_acc_points);
		accPointsAdapter = new AccPointsAdapter(null, lvAccPoints, getLayoutInflater());
		lvAccPoints.setAdapter(accPointsAdapter);
		lvAccPoints.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				addr = accPointsAdapter.getAccPoints().get(position).getIp();
				connect(addr);
			}
		});

		tvChose = (TextView) findViewById(R.id.chose_file_text);
		tvChose.setVisibility(View.GONE);
		tvState = (TextView) findViewById(R.id.state_text);
		etAddr = (EditText) findViewById(R.id.et_content);
		btSend = (Button) findViewById(R.id.btn_sending);
		btSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (stat) {
				case 0:
					addr = etAddr.getText().toString();
					connect(addr);
					break;
				case 1:
					Toast.makeText(mContext, "已链接", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(mContext, "已链接", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		});
		tvChose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				chooseFile();
			}
		});

		//搜索可连接点
		doSearchPoint();
	}

	public void doSearchPoint(){
		//MyThreadPool pool = new MyThreadPool();
		SearchAsyn searchAsyn = new SearchAsyn();
		searchAsyn.execute();
	}

	//完成192.168.0.1-192.168.2.254的udp消息广播
	class SearchAsyn extends AsyncTask<Void, Void, Void>{
		private Dialog dialog;

		public SearchAsyn(){
			dialog = Tools.createLoadingDialog(mContext, "正在搜索连接点...");
		}

		@Override
		protected void onPreExecute() {
			if (dialog!=null)
				dialog.show();
		}

		@Override
		protected void onPostExecute(Void v){
			if(dialog!=null && dialog.isShowing())
				dialog.dismiss();
		}

		@Override
		protected Void doInBackground(Void... params) {
			SearchPointRunnable runnable = new SearchPointRunnable(mContext);
			new Thread(runnable).start();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private void connect(final String addr) {
		if (netService == null)
			return;

		// if(MySocketFactory.getInstance().isSocketExist(addr))
		// {
		// tvState.setText("已经链接到:" + addr);
		// return;
		// }

		tvState.setText("正在链接中...");

		//addr = etAddr.getText().toString();
		if (addr != null && !addr.equals("")) {
			new Thread() {
				public void run() {
					try {
						int re = netService.connect(addr);
						Message msg = new Message();
						msg.what = MSG_CONNECTED;
						msg.obj = re;
						handler.sendMessage(msg);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
			;
		}
	}

	private void chooseFile() {
		Intent intent = new Intent();
		/* ����Pictures����Type�趨Ϊimage */
		// intent.setType("image/*");
		intent.setType("*/*");// ���ļ�Ŀ¼
		/* ʹ��Intent.ACTION_GET_CONTENT���Action */
		intent.setAction(Intent.ACTION_GET_CONTENT);
		/* ȡ����Ƭ�󷵻ر����� */
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Uri uri = data.getData();
			new SendFileAsyn(uri).execute();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == MSG_CONNECTED) {
				int re = (Integer) msg.obj;
				if (re == 0) {
					stat = 2;
					etAddr.setVisibility(View.GONE);
					tvState.setText("链接到:" + addr);
					tvChose.setVisibility(View.VISIBLE);
				}
			}
			else if(msg.what == MSG_TARGET_REFUSE){
				Bundle b = msg.getData();
				tvState.setText("对方拒绝接收该文件");
			}
			else if(msg.what == MSG_SEND_FIN){
				stat = 2;
				tvState.setText("发送成功");
			}
//			else if(msg.what == MSG_SEND_ERROR){
//				int errCode = msg.getData().getInt("errCode");
//				if (errCode == -1) {
//					stat = 2;
//					tvState.setText("发送失败");
//				} else if (errCode == -2) {
//					stat = 2;
//					tvState.setText("文件夹不能发送");
//				} else if (errCode == -3) {
//					stat = 0;
//					tvState.setText("链接断开，重新链接！");
//					tvChose.setVisibility(View.GONE);
//					etAddr = (EditText) findViewById(R.id.et_content);
//				} else if (errCode == -4) {
//					stat = 0;
//					tvState.setText("对方掉线，请重新链接！");
//					tvChose.setVisibility(View.GONE);
//					etAddr.setVisibility(View.VISIBLE);
//				}
//			}
			//有可连接点到来
			else if(msg.what == MSG_POINT_AVAILABLE){
				ArrayList<UserMode> addrs = msg.getData().getParcelableArrayList("addrs");
				for (int i = 0; i < addrs.size(); i++) {
					Log.i(Tag, "可连接点:" + addrs.get(i).getIp() + ", name:" + addrs.get(i).getName());
				}

				accPointsAdapter.setAccPoints(addrs);
				accPointsAdapter.notifyDataSetChanged();
			}
		}
	};

	private void bindMyService() {
		// Intent intent=new Intent("com.dongzi.IStockQuoteService");
		Intent intent = new Intent(mContext, NETService.class);
		// startService(intent);

		bindService(intent, conn, mContext.BIND_AUTO_CREATE);
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			netService = (NETInterface) NETInterface.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
		}
	};

	private class SendFileAsyn extends AsyncTask<Void, Void, Integer> {
		private Uri fileUri;
		private String fileName;
		private String filePath;
		private int re = -1;

		public SendFileAsyn(Uri uri) {
			fileUri = uri;
			filePath = Tools.getRealFilePath(mContext, fileUri);
			fileName = Tools.getFileName(filePath);
			Log.e(Tag, "文件路径:" + filePath);
		}

		@Override
		protected void onPreExecute() {
			tvState.setText("正在发送:" + fileName);
		}

		@Override
		protected void onPostExecute(Integer re) {
			if (re == -1) {
				stat = 2;
				tvState.setText("发送失败:" + fileName);
			} else if (re == -2) {
				stat = 2;
				tvState.setText("文件夹不能发送");
			} else if (re == -3) {
				stat = 0;
				tvState.setText("链接断开，重新链接！");
				tvChose.setVisibility(View.GONE);
				etAddr = (EditText) findViewById(R.id.et_content);
			} else if (re == -4) {
				stat = 0;
				tvState.setText("对方掉线，请重新链接！");
				tvChose.setVisibility(View.GONE);
				etAddr.setVisibility(View.VISIBLE);
			} else if (re == 0) {
				stat = 2;
				tvState.setText("发送成功:" + fileName);
			}
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				stat = 3;
				re = netService.sendFile(addr, filePath);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return re;
		}
	}
}
