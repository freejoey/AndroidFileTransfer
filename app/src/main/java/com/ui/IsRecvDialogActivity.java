package com.ui;

import com.aidl.NETInterface;
import com.aidl.NETService;
import com.example.myfiletransfer.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class IsRecvDialogActivity extends BaseActivity implements OnClickListener {
	private final String Tag = "IsRecvDialogActivity";
	private Context mContext;
	private NETInterface netService = null;

	private TextView tvSender, tvFName;
	private Button btRcv, btRefuse;
	
	private String addr = null;
	private String fileName = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle instance) {
		super.onCreate(instance);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.activity_is_rcv_dialog);
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay(); // Ϊ��ȡ��Ļ����
		android.view.WindowManager.LayoutParams p = getWindow().getAttributes(); 
		p.height = (int) (d.getHeight() * 0.4); // �߶�����Ϊ��Ļ��0.8
		p.width = (int) (d.getWidth() * 0.8); // �������Ϊ��Ļ��0.7
		getWindow().setAttributes(p);
		mContext = this;
		
		bindMyService();

		addr = getIntent().getStringExtra("addr");
		fileName = getIntent().getStringExtra("file");

		tvSender = (TextView) findViewById(R.id.tv_file_sender);
		tvFName = (TextView) findViewById(R.id.tv_file_name);
		tvSender.setText(addr);
		tvFName.setText(fileName);

		btRcv = (Button) findViewById(R.id.bt_comfirem);
		btRefuse = (Button) findViewById(R.id.bt_refuse);
		btRcv.setOnClickListener(this);
		btRefuse.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
	}
	
	private void bindMyService() {
		Intent intent = new Intent(mContext, NETService.class);
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btRcv) {
			try {
				netService.isRecvFile(addr, fileName, true);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (v == btRefuse) {
			try {
				netService.isRecvFile(addr, fileName, false);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		finish();
	}

}
