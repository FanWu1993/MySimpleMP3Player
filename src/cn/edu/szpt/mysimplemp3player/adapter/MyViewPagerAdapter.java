package cn.edu.szpt.mysimplemp3player.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyViewPagerAdapter extends FragmentPagerAdapter {
	private ArrayList<Fragment> fragments; // �洢��Ҫ��ӵ� ViewPager �ϵ� Fragment

	public MyViewPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	/**
	 * �Զ���Ĺ��캯��
	 * 
	 * @param fm
	 * @param fragments
	 *            ArrayList<Fragment>
	 */
	public MyViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		return fragments.get(arg0);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fragments.size();
	}
}
