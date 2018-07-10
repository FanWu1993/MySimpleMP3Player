package cn.edu.szpt.mysimplemp3player;

import java.util.ArrayList;

import cn.edu.szpt.mysimplemp3player.adapter.MyViewPagerAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class MainActivity extends FragmentActivity {
	private ViewPager pager;
	private PagerAdapter mAdapter;
	private ArrayList<Fragment> fragments;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pager = (ViewPager) findViewById(R.id.pager);// 初始化控件，获取 ViewPager对象
		fragments = new ArrayList<Fragment>();// 初始化数据
		fragments.add(new MusicPlayFragment());
		fragments.add(new MusicListFragment());
		initViewPager();
	}

	/**
	 * 初始化 ViewPager
	 */
	private void initViewPager() {
		mAdapter = new MyViewPagerAdapter(getSupportFragmentManager(),
				fragments);
		pager.setAdapter(mAdapter);
		pager.setCurrentItem(0);// 设置当前显示的是位置在第一个的 view
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}