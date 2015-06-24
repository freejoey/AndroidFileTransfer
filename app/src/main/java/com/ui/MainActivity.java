package com.ui;

import java.util.ArrayList;
import java.util.List;

import com.example.myfiletransfer.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

public class MainActivity extends BaseFragmentActivity {
	private final String Tag = "MainActivity";
	private Context mContext;

	private ViewPager pager;
	private PagerTabStrip pagerTab;
	private MyPagerAdapter adapter;

	private ArrayList<Fragment> fragmenList = new ArrayList<Fragment>();
	private String[] titles;

	@Override
	protected void onCreate(Bundle saveInstance) {
		super.onCreate(saveInstance);
		setContentView(R.layout.activity_main);

		titles = new String[] { getResources().getString(R.string.sent_file),
				getResources().getString(R.string.received_file) };
		fragmenList.add(new SendFileFragment());
		fragmenList.add(new RecvFileFragment());

		initView();
	}

	private void initView() {
		pager = (ViewPager) findViewById(R.id.viewpager_content);
		adapter = new MyPagerAdapter(getSupportFragmentManager(), fragmenList);
		pager.setAdapter(adapter);

		pagerTab = (PagerTabStrip) findViewById(R.id.pagertab_indicator);
		pagerTab.setTextColor(getResources().getColor(R.color.text_black));
		pagerTab.setBackgroundColor(Color.rgb(220, 220, 220));
	}

	class MyPagerAdapter extends FragmentPagerAdapter {
		private List<Fragment> fragments = new ArrayList<Fragment>();

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		public MyPagerAdapter(FragmentManager fm, List<Fragment> list) {
			super(fm);
			// TODO Auto-generated constructor stub
			this.fragments = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fragments == null ? 0 : fragments.size();
		}

		//***************�������false�Ͳ�����ʾfragment********************
		// @Override
		// public boolean isViewFromObject(View arg0, Object arg1) {
		// // TODO Auto-generated method stub
		// return false;
		// }

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			return fragments.get(arg0);
		}

		@Override
		public CharSequence getPageTitle(int pos) {
			return titles[pos];

		}
	}
}
