package com.ui;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.Constants;
import com.Tools;
import com.example.myfiletransfer.R;

public class RecvFileFragment extends Fragment {
	private final String Tag = "RecvFileFragment";
	private View view;
	private ListView lvFile;

	private LayoutInflater inflater;
	private ArrayList<File> fileList = new ArrayList<File>();
	private MyAdapter adapter;

	public static Handler mHandler = null;
	public static final int MSG_NEW_RCV_FILE = 0;

	@Override
	public void onCreate(Bundle saveInstance) {
		super.onCreate(saveInstance);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup containor,
			Bundle saveInstance) {
		this.inflater = inflater;
		mHandler = handler;
		if (view == null) {
			view = inflater.inflate(R.layout.fragment_recv_file, containor,
					false);
		}
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		lvFile = (ListView) view.findViewById(R.id.lv_recv_file);
		adapter = new MyAdapter();
		lvFile.setAdapter(adapter);
		lvFile.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(fileList.get(position)),
						"*/*");
				startActivity(intent);
			}
		});

		refreshData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler = null;
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NEW_RCV_FILE:
				Log.i(Tag, "更新RecvFileFragment");
				refreshData();
				break;
			}
		}
	};

	public void refreshData() {
		File f = new File(Constants.getAppPath());
		getAllFiles(f);
		if (fileList != null) {
			adapter.setData(fileList);
			adapter.notifyDataSetChanged();
		}
	}

	private void getAllFiles(File directory) {
		File files[] = directory.listFiles();
		if (fileList != null)
			fileList.clear();
		if (files != null) {
			for (File f : files) {

				if (f.isDirectory()) {
					getAllFiles(f);
				} else {

					this.fileList.add(f);
				}
			}
		}
	}

	class MyAdapter extends BaseAdapter {
		private ArrayList<File> fileList = new ArrayList<File>();

		public void setData(ArrayList<File> fileList) {
			this.fileList = fileList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileList == null ? 0 : fileList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return fileList.get(position);
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
				convertView = inflater.inflate(R.layout.item_sent_file, null);
				holder = new ViewHolder();
				holder.tvName = (TextView) convertView
						.findViewById(R.id.tv_name);
				holder.tvAddr = (TextView) convertView
						.findViewById(R.id.tv_addr);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String absName = Tools
					.getFileName(fileList.get(position).getPath());
			holder.tvName.setText(absName);

			return convertView;
		}

	}

	public final class ViewHolder {
		public TextView tvName, tvAddr;
	}
}
