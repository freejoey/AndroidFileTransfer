package com.ui;

import com.example.myfiletransfer.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MyFragmentDialog extends DialogFragment {
	private ListView lvOption;
	private String[] data;
	private LayoutInflater inflater;

	public MyFragmentDialog(Context c, String[] d) {
		this.data = d;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialogfragment_addr_option,
				container);
		this.inflater = inflater;
		
		lvOption = (ListView) view.findViewById(R.id.lv_addr_option);
		ListAdapter adapter = new ListAdapter();
		lvOption.setAdapter(adapter);
		lvOption.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				listener.click(position);
			}
		});
		
		return view;
	}
	
    @Override
    public void onStart()
    {
        super.onStart();
        if (getDialog() == null)
            return;
        // ���ô�����Զ���title��չ���ԡ�
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

	OnItemClicked listener;
	public void setListener(OnItemClicked listener){
		this.listener = listener;
	}
	public interface OnItemClicked{
		public void click(int i);
	}
	
	class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data[position];
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
			if(convertView==null){
				convertView = inflater.inflate(R.layout.item_dialogfragment_option, null);
				holder = new ViewHolder();
				holder.item = (TextView) convertView.findViewById(R.id.tv_dialogfragment_item_option);
				convertView.setTag(holder);
			}
			else{
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.item.setText(data[position]);
			
			return convertView;
		}
	}
	
	public final class ViewHolder{
		public TextView item;
	}
	
}
