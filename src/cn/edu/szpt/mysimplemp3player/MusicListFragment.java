package cn.edu.szpt.mysimplemp3player;

import cn.edu.szpt.mysimplemp3player.adapter.MusicListAdapter;
import cn.edu.szpt.mysimplemp3player.constants.SMPConstants;
import cn.edu.szpt.mysimplemp3player.service.PlayMusicService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class MusicListFragment extends Fragment {
	private View view;// ����ҳ��
	private Context context;
	private ListView listview;
	MusicListAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (view == null) {
			// ���� fragment_musiclist.xml �����ļ�
			view = inflater.inflate(R.layout.fragment_musiclist, container,
					false);
		}
		// ��ȡ������
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);// ���Ƴ� view
		}
		setData();
		return view;
	}

	private void setData() {
		context = this.getActivity().getApplicationContext();
		listview = (ListView) view.findViewById(R.id.musiclist);
		// ���� ContentProvider ��ѯ�洢���е������ļ�
		// ��ѯ�Ľ�������α� c ��
		Cursor c = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.DATA }, null, null, null);

		c.moveToFirst();
		// ����������������
		adapter = new MusicListAdapter(context, c);
		// �ø���������� listView
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				Intent i = new Intent(context, PlayMusicService.class);
				i.putExtra("CMD", SMPConstants.CMD_PLAYATID);
				ImageView img = (ImageView) arg1.findViewById(R.id.listitem);
				i.putExtra("MUSICID", img.getTag().toString());
				context.startService(i);
				adapter.setItemIcon(position);
				adapter.notifyDataSetChanged();
			}
		});

	}
}
