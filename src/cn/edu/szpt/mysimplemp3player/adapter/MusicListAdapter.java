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
private Context myContext; // �����Ķ���
private Cursor myCur; // �α���󣬴洢��������
private int pos = -1; // ���ڲ��ŵ�λ�����
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
	// ���ز����ļ� musicitem.xml
	convertView = LayoutInflater.from(myContext).inflate(
	R.layout.musicitem, null);
	// ���α����ƶ���ָ����λ��
	myCur.moveToPosition(position);
	// ��ȡ��ʾ�������Ƶ� TextView ���
	TextView tv_music = (TextView) convertView.findViewById(R.id.music);
	//������������ʾ����Ӧ�������
	String musicTitle = myCur.getString(0).trim();
	tv_music.setText(musicTitle);
	//�����ݳ�����Ϣ
	TextView tv_singer = (TextView) convertView.findViewById(R.id.singer);
	if (myCur.getString(2).equals("<unknown>")) {
	tv_singer.setText("δ֪������");
	} else {
	tv_singer.setText(myCur.getString(2));
	}
	//��ʾ����ʱ��
	TextView tv_time = (TextView) convertView.findViewById(R.id.time);
	//�������ʱ��ת��Ϊʱʱ���ַ֣��������ʽ
		tv_time.setText(Util.toTime(myCur.getInt(1)));
	//��ȡ��ʾ�б�ͼ������
	ImageView img = (ImageView) convertView.findViewById(R.id.listitem);
	 //����_ID ֵ
	img.setTag(myCur.getInt(3));
	//����ǰλ�����Ϊ���ڲ��ŵ���ţ�����ʾ����ͼ�꣬������ʾһ��ͼ��
	if (position == pos) {
	img.setImageResource(R.drawable.isplaying);
	} else {
	img.setImageResource(R.drawable.item);
	}
	return convertView;
	}
	//������ʾ����ͼ������
	public void setItemIcon(int position){
	pos = position;
	}
	}