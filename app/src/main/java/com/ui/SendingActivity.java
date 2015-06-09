package com.ui;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.Constants;
import com.MyApplication;
import com.MyThreadPool;
import com.Tools;
import com.aidl.NETInterface;
import com.aidl.NETService;
import com.example.myfiletransfer.R;
import com.net.ClientThread;
import com.net.MySocketFactory;

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
import android.widget.Button;
import android.widget.EditText;
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

	private String addr = null;

	public static final String ADDR_KEY = "_addr_key";
	public static final int MSG_CONNECTED = 0;
	public static final int MSG_TARGET_REFUSE = 1;
	public static final int MSG_SEND_FIN = 2;
	public static final int MSG_SEND_ERROR = 3;

	/*
	 * ��ǰ״̬: 0: ������ 1������ 2�����ڷ��� 3���������
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
					connect();
					break;
				case 1:
					Toast.makeText(mContext, "������", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(mContext, "������", Toast.LENGTH_SHORT).show();
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

		// ��һ���Ѿ����ӵĵ�ַ���ļ�
		if (addr != null && !addr.equals("")) {
			int re = 0;
			Message msg = new Message();
			msg.what = MSG_CONNECTED;
			msg.obj = re;
			handler.sendMessage(msg);
		}

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
		MyThreadPool pool = null;

		public SearchAsyn(){
			dialog = Tools.createLoadingDialog(mContext, "正在搜索连接点...");
			pool = new MyThreadPool();
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

//			Log.e(Tag, "可连接点:");
//			HashMap<String, ClientThread> conns = MySocketFactory.getInstance().getAllConn();
//			for(int i=0; i<conns.size(); i++){
//				Log.e(Tag, conns.get(i).getName());
//			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			SearchPointRunnable runnable = new SearchPointRunnable(mContext);
			new Thread(runnable).start();

//			String head = "192.168.";
//			String s1,s2;
//			for (int i=0; i<3; i++){
//				s1 = head + String.valueOf(i) + ".";
//				for (int j=1; j<255; j++){
//					s2 = s1 + String.valueOf(j);
//					SearchPointRunnable runnable = new SearchPointRunnable(s2, mContext);
//					runnable.setListener(new SearchPointRunnable.ConnListener() {
//						@Override
//						public void onAccessable(String addr) {
//							//可连接
//							Log.i(Tag, "可连接点--->" + addr);
//						}
//
//						@Override
//						public void onUnaccess() {
//							//不可连接
//						}
//					});
//					pool.execute(runnable);
//				}
//			}
			return null;
		}
	}

	private void connect() {
		if (netService == null)
			return;

		// if(MySocketFactory.getInstance().isSocketExist(addr))
		// {
		// tvState.setText("�Ѿ����ӵ�:" + addr);
		// return;
		// }

		tvState.setText("����������...");

		addr = etAddr.getText().toString();
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
					tvState.setText("���ӵ�:" + addr);
					tvChose.setVisibility(View.VISIBLE);
				}
			}
			else if(msg.what == MSG_TARGET_REFUSE){
				Bundle b = msg.getData();
				tvState.setText("�Է��ܾ����ո��ļ�");
			}
			else if(msg.what == MSG_SEND_FIN){
				stat = 2;
				tvState.setText("���ͳɹ�");
			}
			else if(msg.what == MSG_SEND_ERROR){
				int errCode = msg.getData().getInt("errCode");
				if (errCode == -1) {
					stat = 2;
					tvState.setText("����ʧ��");
				} else if (errCode == -2) {
					stat = 2;
					tvState.setText("�ļ��в��ܷ���");
				} else if (errCode == -3) {
					stat = 0;
					tvState.setText("���ӶϿ����������ӣ�");
					tvChose.setVisibility(View.GONE);
					etAddr = (EditText) findViewById(R.id.et_content);
				} else if (errCode == -4) {
					stat = 0;
					tvState.setText("�Է����ߣ����������ӣ�");
					tvChose.setVisibility(View.GONE);
					etAddr.setVisibility(View.VISIBLE);
				}
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
			filePath = Tools.getRealFilePath(mContext, uri);
			fileName = Tools.getFileName(filePath);
			Log.e(Tag, "�ļ�·��:" + filePath);
		}

		@Override
		protected void onPreExecute() {
			tvState.setText("���ڷ���:" + fileName);
		}

		@Override
		protected void onPostExecute(Integer re) {
			if (re == -1) {
				stat = 2;
				tvState.setText("����ʧ��:" + fileName);
			} else if (re == -2) {
				stat = 2;
				tvState.setText("�ļ��в��ܷ���");
			} else if (re == -3) {
				stat = 0;
				tvState.setText("���ӶϿ����������ӣ�");
				tvChose.setVisibility(View.GONE);
				etAddr = (EditText) findViewById(R.id.et_content);
			} else if (re == -4) {
				stat = 0;
				tvState.setText("�Է����ߣ����������ӣ�");
				tvChose.setVisibility(View.GONE);
				etAddr.setVisibility(View.VISIBLE);
			} else if (re == 0) {
				stat = 2;
				tvState.setText("���ͳɹ�:" + fileName);
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
