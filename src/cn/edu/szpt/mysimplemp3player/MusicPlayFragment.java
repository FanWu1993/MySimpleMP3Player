package cn.edu.szpt.mysimplemp3player;

import cn.edu.szpt.mysimplemp3player.constants.SMPConstants;
import cn.edu.szpt.mysimplemp3player.service.PlayMusicService;
import cn.edu.szpt.mysimplemp3player.util.Util;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MusicPlayFragment extends Fragment implements OnClickListener {
	private View view;// ����ҳ��
	private Context context;
	// ���Ű�ť
	private ImageButton btnPlay;
	// ��һ�װ�ť
	private ImageButton btnPrev;
	// ��һ�װ�ť
	private ImageButton btnNext;
	// ��ʾ��������
	private TextView tvSongName;
	// ��ʾ����ʱ��
	private TextView tvDuration;
	// ��ʾ���
	private TextView tvLrc;
	// ��ʾ�������ŵ�ǰʱ��
	private TextView tvPlayTime;
	// ��ʾ������
	private SeekBar sbSong;
	// ��ʾר������
	private ImageView imgSong;
	// ��¼������״̬
	private int MpStatus;
	// ��¼��ǰ���������ݿ��е�_ID ֵ
	public static int currentMusicID = -1;
	// ָʾ�����Ƿ���Ҫˢ��
	private boolean needRefresh = true;
	private StatusReceiver statusReceiver;
	private LrcReceiver lrcReceiver;
	private PrgReceiver prgReceiver;

	class PrgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// ��ȡ�㲥�д��Ľ�������Ϣ
			int time = intent.getIntExtra("PROGRESS", 0);
			// ���ý�������λ��
			sbSong.setProgress(time);
			// ��ʾ��ǰ����ʱ��
			tvPlayTime.setText(Util.toTime(time));
		}
	}

	class LrcReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// ��ȡ�㲥�д��ĸ����Ϣ
			String msg = intent.getStringExtra("LRC");
			// ��ʾ�����Ϣ
			tvLrc.setText(msg);
		}
	}

	// �� PlayMusicService ����һ����ѯ״̬����
	private void getMPStatus() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_GETINFORM);
		context.startService(i);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// ע��㲥
		context.registerReceiver(statusReceiver, new IntentFilter(
				SMPConstants.ACT_SERVICE_REQUEST_BROADCAST));
		context.registerReceiver(lrcReceiver, new IntentFilter(
				SMPConstants.ACT_LRC_RETURN_BROADCAST));
		context.registerReceiver(prgReceiver, new IntentFilter(
				SMPConstants.ACT_PROGRESS_RETURN_BROADCAST));
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// ȡ��ע��㲥
		context.unregisterReceiver(statusReceiver);
		context.unregisterReceiver(lrcReceiver);
		context.unregisterReceiver(prgReceiver);
		needRefresh = true;
	}

	private void progress_change(int progress) {
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_CHANDEPROGRESS);
		i.putExtra("PROGRESS", progress);
		context.startService(i);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (view == null) {
			// ���� fragment_music.xml �����ļ�
			view = inflater.inflate(R.layout.fragment_music, container, false);
		}
		// ��ȡ������
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);// ���Ƴ� view
		}
		context = this.getActivity().getApplicationContext();
		btnPlay = (ImageButton) view.findViewById(R.id.playBtn);
		btnPrev = (ImageButton) view.findViewById(R.id.prevBtn);
		btnNext = (ImageButton) view.findViewById(R.id.nextBtn);
		tvSongName = (TextView) view.findViewById(R.id.name);
		tvDuration = (TextView) view.findViewById(R.id.duration);
		tvPlayTime = (TextView) view.findViewById(R.id.playtime);
		tvLrc = (TextView) view.findViewById(R.id.lrc);
		sbSong = (SeekBar) view.findViewById(R.id.seekbar);
		imgSong = (ImageView) view.findViewById(R.id.showpic);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnPrev.setOnClickListener(this);
		statusReceiver = new StatusReceiver();
		lrcReceiver = new LrcReceiver();
		prgReceiver = new PrgReceiver();
		getMPStatus();
		sbSong.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		return view;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.playBtn:
			switch (MpStatus) {
			case SMPConstants.STATUS_PAUSE:
				continueMusic();
				break;
			case SMPConstants.STATUS_PLAY:
				pauseMusic();
				break;
			case SMPConstants.STATUS_STOP:
				playMusic();
				break;
			default:
				break;
			}
			break;
		case R.id.prevBtn:
			prevMusic();
			break;
		case R.id.nextBtn:
			nextMusic();
			break;
		default:
			break;
		}
	}

	// ������ͣ����
	private void pauseMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_PAUSE);
		context.startService(i);
		MpStatus = SMPConstants.STATUS_PAUSE;
		// �޸İ�ť��ͼƬ
		btnPlay.setBackgroundResource(R.drawable.play_selector);
	}

	// ���ͼ�������
	private void continueMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_CONTINUE);
		context.startService(i);
		MpStatus = SMPConstants.STATUS_PLAY;
		// �޸İ�ť��ͼƬ
		btnPlay.setBackgroundResource(R.drawable.pause_selector);
	}

	// ���Ͳ�������
	private void playMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_PLAY);
		context.startService(i);

		MpStatus = SMPConstants.STATUS_PLAY;
		// �޸İ�ť��ͼƬ
		btnPlay.setBackgroundResource(R.drawable.pause_selector);
		tvLrc.setText("");
	}

	// ��һ��
	private void nextMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_NEXT);
		context.startService(i);
		MpStatus = SMPConstants.STATUS_PLAY;
		// �޸İ�ť��ͼƬ
		btnPlay.setBackgroundResource(R.drawable.pause_selector);
		tvLrc.setText("");
	}

	// ��һ��
	private void prevMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_PREV);
		context.startService(i);
		MpStatus = SMPConstants.STATUS_PLAY;
		// �޸İ�ť��ͼƬ
		btnPlay.setBackgroundResource(R.drawable.pause_selector);
		tvLrc.setText("");
	}

	class StatusReceiver extends BroadcastReceiver {
		/**
		 * ����ר�� id ��ѯר������ͼƬ������·��
		 * 
		 * @param album_id
		 * @return album_art
		 */
		private String getAlbumArt(int album_id) {
			// ����ͼƬ����·��ǰ׺
			String mUriAlbums = "content://media/external/audio/albums";
			// ��ѯ����ͼƬ����·��
			String[] projection = new String[] { "album_art" };
			Cursor cur = context.getContentResolver().query(
					Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
					projection, null, null, null);
			String album_art = null;
			if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
				cur.moveToNext();
				// ��ȡͼƬ��·��
				album_art = cur.getString(0);
			}
			cur.close();
			cur = null;
			return album_art;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// ��¼��ǰ��״̬��Ϣ
			int prevStatus = MpStatus;
			// ��ȡ��̨���ص��µ�״̬��Ϣ
			MpStatus = intent.getExtras().getInt("MPSTATUS");
			// ��¼��ǰ�ĸ���_ID ֵ
			int prevID = currentMusicID;
			// ��ȡ��̨���ص��¸���_ID ֵ
			currentMusicID = intent.getIntExtra("MUSICID", -1);
			// ��������б仯���ߵ��������ͣ״̬��������ʱ������½����ϵ���ʾ��Ϣ
			if (currentMusicID != prevID || needRefresh) {
				Uri MUSIC_URL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				Cursor mc = context.getContentResolver().query(MUSIC_URL,
						SMPConstants.mCursorCols, "_id=" + currentMusicID,
						null, null);
				if (!mc.moveToFirst()) {
					Log.i("Err", "��ǰ����������");
					return;
				}
				// ���ø�����
				tvSongName.setText(mc.getString(mc
						.getColumnIndex(MediaStore.Audio.Media.TITLE)));
				// ���ø�������
				tvDuration.setText(Util.toTime(mc.getInt(mc
						.getColumnIndex(MediaStore.Audio.Media.DURATION))));
				// ���ý���������󳤶�
				sbSong.setIndeterminate(false);
				sbSong.setMax(mc.getInt(mc
						.getColumnIndex(MediaStore.Audio.Media.DURATION)));
				// ��ȡר�� id
				int album_id = mc.getInt(mc
						.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
				Bitmap bm = null;
				// ��ȡר��ͼƬ·��
				String albumArt = getAlbumArt(album_id);
				// ����ܹ��ҵ�ר����������ʾ��������ʾĬ��ͼƬ
				if (albumArt != null) {
					try {
						bm = BitmapFactory.decodeFile(albumArt);
					} catch (Exception e) {
						Log.i("Err", "��ר���������");
					}
					if (bm != null) {
						BitmapDrawable bmpDraw = new BitmapDrawable(
								getResources(), bm);
						imgSong.setImageDrawable(bmpDraw);
					} else {
						imgSong.setImageResource(R.drawable.nopic);
					}
				} else {
					imgSong.setImageResource(R.drawable.nopic);
				}
			}
			// ���ǰ���״̬��һ�£�����ݺ�̨״̬�޸�ǰ̨�Ľ���
			if (MpStatus != prevStatus) {
				switch (MpStatus) {
				case SMPConstants.STATUS_PLAY:
					btnPlay.setBackgroundResource(R.drawable.pause_selector);
					break;
				case SMPConstants.STATUS_PAUSE:
					btnPlay.setBackgroundResource(R.drawable.play_selector);
					break;
				case SMPConstants.STATUS_STOP:
					btnPlay.setBackgroundResource(R.drawable.play_selector);
					break;
				default:
					break;
				}
			}
			needRefresh = false;
		}
	}

}