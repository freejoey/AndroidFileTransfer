package com.ui;

import java.io.File;
import java.util.ArrayList;

import com.Constants;
import com.Tools;
import com.andexert.library.RippleView;
import com.example.myfiletransfer.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SendFileFragment extends Fragment {
	private View view;
	private ListView lvFile;
	private Button btSend;
	private RippleView rpBtn;
	private boolean btSendClicked = false;
	
	@Override
	public void onCreate(Bundle saveInstance) {
		super.onCreate(saveInstance);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			//相当于Fragment的onResume
			btSendClicked = false;
		} else {
			//相当于Fragment的onPause
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup containor,
			Bundle saveInstance) {
		if (view == null) {
			view = inflater.inflate(R.layout.fragment_send_file, containor,
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

		rpBtn = (RippleView) view.findViewById(R.id.rp_btn_sending);
		rpBtn.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {

			@Override
			public void onComplete(RippleView rippleView) {
				if(btSendClicked){
					Bundle b = new Bundle();
					b.putString(SendingActivity.ADDR_KEY, null);
					Intent i = new Intent(getActivity(), SendingActivity.class);
					i.putExtras(b);
					getActivity().startActivity(i);
				}
			}

		});;
		btSend = (Button) view.findViewById(R.id.btn_sending);
		btSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btSendClicked = true;
			}
		});
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
}
