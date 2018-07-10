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
	private View view;// 缓存页面
	private Context context;
	// 播放按钮
	private ImageButton btnPlay;
	// 上一首按钮
	private ImageButton btnPrev;
	// 下一首按钮
	private ImageButton btnNext;
	// 显示歌曲名称
	private TextView tvSongName;
	// 显示歌曲时长
	private TextView tvDuration;
	// 显示歌词
	private TextView tvLrc;
	// 显示歌曲播放当前时间
	private TextView tvPlayTime;
	// 显示进度条
	private SeekBar sbSong;
	// 显示专辑封面
	private ImageView imgSong;
	// 记录播放器状态
	private int MpStatus;
	// 记录当前歌曲在数据库中的_ID 值
	public static int currentMusicID = -1;
	// 指示界面是否需要刷新
	private boolean needRefresh = true;
	private StatusReceiver statusReceiver;
	private LrcReceiver lrcReceiver;
	private PrgReceiver prgReceiver;

	class PrgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// 获取广播中带的进度条信息
			int time = intent.getIntExtra("PROGRESS", 0);
			// 设置进度条的位置
			sbSong.setProgress(time);
			// 显示当前播放时间
			tvPlayTime.setText(Util.toTime(time));
		}
	}

	class LrcReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// 获取广播中带的歌词信息
			String msg = intent.getStringExtra("LRC");
			// 显示歌词信息
			tvLrc.setText(msg);
		}
	}

	// 向 PlayMusicService 发送一条查询状态命令
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
		// 注册广播
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
		// 取消注册广播
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
			// 加载 fragment_music.xml 布局文件
			view = inflater.inflate(R.layout.fragment_music, container, false);
		}
		// 获取父容器
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);// 先移除 view
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

	// 发送暂停命令
	private void pauseMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_PAUSE);
		context.startService(i);
		MpStatus = SMPConstants.STATUS_PAUSE;
		// 修改按钮的图片
		btnPlay.setBackgroundResource(R.drawable.play_selector);
	}

	// 发送继续命令
	private void continueMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_CONTINUE);
		context.startService(i);
		MpStatus = SMPConstants.STATUS_PLAY;
		// 修改按钮的图片
		btnPlay.setBackgroundResource(R.drawable.pause_selector);
	}

	// 发送播放命令
	private void playMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_PLAY);
		context.startService(i);

		MpStatus = SMPConstants.STATUS_PLAY;
		// 修改按钮的图片
		btnPlay.setBackgroundResource(R.drawable.pause_selector);
		tvLrc.setText("");
	}

	// 下一首
	private void nextMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_NEXT);
		context.startService(i);
		MpStatus = SMPConstants.STATUS_PLAY;
		// 修改按钮的图片
		btnPlay.setBackgroundResource(R.drawable.pause_selector);
		tvLrc.setText("");
	}

	// 上一首
	private void prevMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, PlayMusicService.class);
		i.putExtra("CMD", SMPConstants.CMD_PREV);
		context.startService(i);
		MpStatus = SMPConstants.STATUS_PLAY;
		// 修改按钮的图片
		btnPlay.setBackgroundResource(R.drawable.pause_selector);
		tvLrc.setText("");
	}

	class StatusReceiver extends BroadcastReceiver {
		/**
		 * 根据专辑 id 查询专辑封面图片的所在路径
		 * 
		 * @param album_id
		 * @return album_art
		 */
		private String getAlbumArt(int album_id) {
			// 封面图片所在路径前缀
			String mUriAlbums = "content://media/external/audio/albums";
			// 查询封面图片所在路径
			String[] projection = new String[] { "album_art" };
			Cursor cur = context.getContentResolver().query(
					Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
					projection, null, null, null);
			String album_art = null;
			if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
				cur.moveToNext();
				// 获取图片的路径
				album_art = cur.getString(0);
			}
			cur.close();
			cur = null;
			return album_art;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// 记录当前的状态信息
			int prevStatus = MpStatus;
			// 获取后台传回的新的状态信息
			MpStatus = intent.getExtras().getInt("MPSTATUS");
			// 记录当前的歌曲_ID 值
			int prevID = currentMusicID;
			// 获取后台传回的新歌曲_ID 值
			currentMusicID = intent.getIntExtra("MUSICID", -1);
			// 如果歌曲有变化或者当界面从暂停状态重新载入时，则更新界面上的显示信息
			if (currentMusicID != prevID || needRefresh) {
				Uri MUSIC_URL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				Cursor mc = context.getContentResolver().query(MUSIC_URL,
						SMPConstants.mCursorCols, "_id=" + currentMusicID,
						null, null);
				if (!mc.moveToFirst()) {
					Log.i("Err", "当前歌曲不存在");
					return;
				}
				// 设置歌曲名
				tvSongName.setText(mc.getString(mc
						.getColumnIndex(MediaStore.Audio.Media.TITLE)));
				// 设置歌曲长度
				tvDuration.setText(Util.toTime(mc.getInt(mc
						.getColumnIndex(MediaStore.Audio.Media.DURATION))));
				// 设置进度条的最大长度
				sbSong.setIndeterminate(false);
				sbSong.setMax(mc.getInt(mc
						.getColumnIndex(MediaStore.Audio.Media.DURATION)));
				// 获取专辑 id
				int album_id = mc.getInt(mc
						.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
				Bitmap bm = null;
				// 获取专辑图片路径
				String albumArt = getAlbumArt(album_id);
				// 如果能够找到专辑封面则显示，否则显示默认图片
				if (albumArt != null) {
					try {
						bm = BitmapFactory.decodeFile(albumArt);
					} catch (Exception e) {
						Log.i("Err", "打开专辑封面出错");
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
			// 如果前后的状态不一致，则根据后台状态修改前台的界面
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