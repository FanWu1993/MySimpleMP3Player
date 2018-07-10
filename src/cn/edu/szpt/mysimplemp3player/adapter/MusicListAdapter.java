package cn.edu.szpt.mysimplemp3player.adapter;

import cn.edu.szpt.mysimplemp3player.R;
import cn.edu.szpt.mysimplemp3player.util.Util;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter {
private Context myContext; // 上下文对象
private Cursor myCur; // 游标对象，存储歌曲数据
private int pos = -1; // 正在播放的位置序号
public MusicListAdapter(Context con, Cursor cur) {
myContext = con;
myCur = cur;
}
@Override
public int getCount() {
// TODO Auto-generated method stub
return myCur.getCount();
}
@Override
public Object getItem(int position) {
// TODO Auto-generated method stub
return position;
}
@Override
public long getItemId(int position) {
	// TODO Auto-generated method stub
	return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	// TODO Auto-generated method stub
	// 加载布局文件 musicitem.xml
	convertView = LayoutInflater.from(myContext).inflate(
	R.layout.musicitem, null);
	// 在游标中移动到指定的位置
	myCur.moveToPosition(position);
	// 获取显示歌曲名称的 TextView 组件
	TextView tv_music = (TextView) convertView.findViewById(R.id.music);
	//将歌曲名称显示在相应的组件中
	String musicTitle = myCur.getString(0).trim();
	tv_music.setText(musicTitle);
	//设置演唱者信息
	TextView tv_singer = (TextView) convertView.findViewById(R.id.singer);
	if (myCur.getString(2).equals("<unknown>")) {
	tv_singer.setText("未知艺术家");
	} else {
	tv_singer.setText(myCur.getString(2));
	}
	//显示歌曲时长
	TextView tv_time = (TextView) convertView.findViewById(R.id.time);
	//将毫秒的时长转换为时时：分分：秒秒的形式
		tv_time.setText(Util.toTime(myCur.getInt(1)));
	//获取显示列表图标的组件
	ImageView img = (ImageView) convertView.findViewById(R.id.listitem);
	 //保存_ID 值
	img.setTag(myCur.getInt(3));
	//当当前位置序号为正在播放的序号，则显示播放图标，否则显示一般图标
	if (position == pos) {
	img.setImageResource(R.drawable.isplaying);
	} else {
	img.setImageResource(R.drawable.item);
	}
	return convertView;
	}
	//设置显示播放图标的序号
	public void setItemIcon(int position){
	pos = position;
	}
	}