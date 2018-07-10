package cn.edu.szpt.mysimplemp3player.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cn.edu.szpt.mysimplemp3player.constants.SMPConstants;
import cn.edu.szpt.mysimplemp3player.lrc.LrcBean;
import cn.edu.szpt.mysimplemp3player.lrc.LrcProcessor;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PlayMusicService extends Service {

	// 保存 MediaPlayer 对象，用以播放音乐
	private MediaPlayer mp;
	// 用以记录播放器的状态
	private int MpStatus;
	// 游标对象，用以存放查询的歌曲信息
	private Cursor mCursor;
	// 当前播放的歌曲在吗 Cursor 中的序号
	private int currentMusicIndex = 0;
	// 保存每条歌词的时间和内容
	private ArrayList<LrcBean> lrcs;
	// 下一条歌词显示的时间
	private int nextTimeMil = 0;
	// 歌词 Arraylist 中的序号
	private int LrcPos;
	// 歌词内容
	private String message;
	// 用以调度歌词线程
	private Handler lrcHandler = new Handler();
	//
	private lrcCallBack r = null;
	private prgCallBack pr = null;

	// 用以调度进度线程
	private Handler prgHandler = new Handler();

	class prgCallBack implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int time = mp.getCurrentPosition();
			Intent i = new Intent(SMPConstants.ACT_PROGRESS_RETURN_BROADCAST);
			i.putExtra("PROGRESS", time);
			sendBroadcast(i);
			// 每隔 300 毫秒发送一次
			prgHandler.postDelayed(this, 300);
		}

	}

	private void initLrc(String lrcPath) {
		// TODO Auto-generated method stub
		InputStream in;
		try {
			// 判断指定文件的编码格式
			String charset = LrcProcessor.getCharSet(new FileInputStream(
					lrcPath));
			// 解析歌词文件
			LrcProcessor lrcProc = new LrcProcessor();
			in = new FileInputStream(lrcPath);
			lrcs = lrcProc.process(in, charset);
			if (r != null) {
				lrcHandler.removeCallbacks(r);
			}
			// 实例化线程对象
			r = new lrcCallBack(lrcs);
			if (pr != null) {
				prgHandler.removeCallbacks(pr);
			}
			// 创建进度条线程对象
			pr = new prgCallBack();
			nextTimeMil = 0;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	// 获取歌曲列表
	private Cursor getMusicList() {
		Uri MUSIC_URL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		return getContentResolver().query(MUSIC_URL, SMPConstants.mCursorCols,
				null, null, null);
	}

	// 根据位置来获取歌曲文件路径
	public String getDateByPosition(Cursor c, int position) {
		c.moveToPosition(position);
		int dataColumn = c.getColumnIndex(MediaStore.Audio.Media.DATA);
		String data = c.getString(dataColumn);
		return data;
	}

	// 根据歌曲所在的序号，获取其对应的_ID 值
	private int getMusicIDByPosition(Cursor c, int position) {
		if (c.moveToPosition(position))
			return c.getInt(0);
		else
			return -1;
	}

	// 通过广播向前台发送状态信息
	private void sendMPInform() {
		// TODO Auto-generated method stub
		// 设置广播 Intent
		Intent i = new Intent(SMPConstants.ACT_SERVICE_REQUEST_BROADCAST);
		i.putExtra("CMD", SMPConstants.RETURN_STATUS);
		// 传送的状态信息
		i.putExtra("MPSTATUS", MpStatus);
		// 当前歌曲的_ID 值
		i.putExtra("MUSICID", getMusicIDByPosition(mCursor, currentMusicIndex));
		// 发送广播
		sendBroadcast(i);
	}

	// 暂停播放
	private void pauseMuisc() {
		// TODO Auto-generated method stub
		mp.pause();
		lrcHandler.removeCallbacks(r);
		prgHandler.removeCallbacks(pr);
		MpStatus = SMPConstants.STATUS_PAUSE;
		sendMPInform();
	}

	// 继续播放
	private void continueMusic() {
		// TODO Auto-generated method stub
		mp.start();
		lrcHandler.post(r);
		prgHandler.post(pr);
		MpStatus = SMPConstants.STATUS_PLAY;
		sendMPInform();
	}

	// 播放
	private void playMusic() {
		// TODO Auto-generated method stub
		String musicPath = getDateByPosition(mCursor, currentMusicIndex);
		try {
			mp.reset();
			mp.setDataSource(musicPath);
			mp.prepare();
			initLrc(musicPath.substring(0, musicPath.length() - 3) + "lrc");
			// 启动歌词线程
			lrcHandler.post(r);
			// 启动进度条线程
			prgHandler.post(pr);
			mp.start();
			MpStatus = SMPConstants.STATUS_PLAY;
			sendMPInform();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 播放上一首，如果已经是第一首则播放最后一首
	private void prevMusic() {
		// TODO Auto-generated method stub
		if (currentMusicIndex <= 0) {
			currentMusicIndex = mCursor.getCount() - 1;
		} else {
			currentMusicIndex--;
		}
		playMusic();
		MpStatus = SMPConstants.STATUS_PLAY;
	}

	// 播放下一首，如果已经是最后一首则播放第一首
	private void nextMusic() {
		// TODO Auto-generated method stub
		if (currentMusicIndex >= mCursor.getCount() - 1) {
			currentMusicIndex = 0;
		} else {
			currentMusicIndex++;
		}
		playMusic();
		MpStatus = SMPConstants.STATUS_PLAY;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// 获取歌曲列表信息，放到 mCursor 中保存
		mCursor = getMusicList();
		// 当前播放位置指向第一首歌曲
		currentMusicIndex = 0;
		// 当前播放器状态设置为 Stop 状态
		MpStatus = SMPConstants.STATUS_STOP;
		// 实例化 MediaPlayer 对象
		mp = new MediaPlayer();
		// 当歌曲播放完后自动播放下一首
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				nextMusic();
			}
		});
		// 对电话的来电状态进行监听
		TelephonyManager telManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		// 注册一个监听器对电话状态进行监听
		telManager.listen(new MyPhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		// 命令信息放在 intent 中的 Extras 中
		int cmd = intent.getExtras().getInt("CMD");
		switch (cmd) {
		case SMPConstants.CMD_PLAY:
			playMusic();
			break;
		case SMPConstants.CMD_PAUSE:
			pauseMuisc();
			break;
		case SMPConstants.CMD_CONTINUE:
			continueMusic();
			break;
		case SMPConstants.CMD_NEXT:
			nextMusic();
			break;
		case SMPConstants.CMD_PREV:
			prevMusic();
			break;
		case SMPConstants.CMD_GETINFORM:
			sendMPInform();
			break;
		case SMPConstants.CMD_CHANDEPROGRESS:
			changPrgPosition(intent.getIntExtra("PROGRESS", 0));
			break;
		case SMPConstants.CMD_PLAYATID:
			playAtID(Integer.parseInt(intent.getStringExtra("MUSICID")));
			break;
		default:
			break;
		}
		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mp != null) {
			mp.stop();
			mp.release();
		}
	}

	class lrcCallBack implements Runnable {
		private ArrayList<LrcBean> lrcList;

		public lrcCallBack(ArrayList<LrcBean> lrcList) {
			this.lrcList = lrcList;
			LrcPos = 0;
		}

		@Override
		public void run() {
			try {
				// 如果首次调用，则获取下一条歌词的显示时间和内容
				if (nextTimeMil == 0) {
					nextTimeMil = lrcList.get(LrcPos).getBeginTime();
					message = lrcList.get(LrcPos).getLrcMsg();
				}
				// 获取当前播放时间
				int time = mp.getCurrentPosition();
				// 如果到了歌词显示的时间，则将歌词通过广播发送出去
				if (time >= nextTimeMil) {
					// 通过广播将歌词发送到前台
					Intent i = new Intent(SMPConstants.ACT_LRC_RETURN_BROADCAST);
					i.putExtra("LRC", message);
					sendBroadcast(i);
					LrcPos++;
					// 获取下条歌词的显示时间
					nextTimeMil = lrcList.get(LrcPos).getBeginTime();
					// 获取下条歌词的内容
					message = lrcList.get(LrcPos).getLrcMsg();
				}
				// 如果时间没有超过歌曲长度，则 10 毫秒后再次运行该线程
				if (time < mp.getDuration()) {
					lrcHandler.postDelayed(this, 10);
				}
			} catch (Exception ex) {
				// Log.i("LrcErr",ex.getMessage());
			}
		}
	}

	private void changPrgPosition(int msec) {
		// TODO Auto-generated method stub
		// 如果没有开始播放，则准备播放
		if (r == null) {
			String musicPath = getDateByPosition(mCursor, currentMusicIndex);
			mp.reset();
			try {
				mp.setDataSource(musicPath);
				mp.prepare();
				initLrc(musicPath.substring(0, musicPath.length() - 3) + "lrc");
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 跳转到指定的位置
		mp.seekTo(msec);
		// 根据歌曲跳转的位置，跳转到歌词对应的位置
		changeLrcPosition(Long.valueOf(msec));
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 继续播放
		continueMusic();
	}

	private void changeLrcPosition(Long msec) {
		for (int i = 0; i < lrcs.size(); i++) {
			if (lrcs.get(i).getBeginTime() >= msec) {
				LrcPos = i;
				nextTimeMil = lrcs.get(i - 1).getBeginTime();
				message = lrcs.get(i - 1).getLrcMsg();
				Intent intent = new Intent(
						SMPConstants.ACT_LRC_RETURN_BROADCAST);
				intent.putExtra("LRC", message);
				sendBroadcast(intent);
				nextTimeMil = lrcs.get(i).getBeginTime();
				message = lrcs.get(i).getLrcMsg();
				break;
			}
		}
	}

	// 根据歌曲_ID 值，获取其对应的序号
	private int getMusicPositionByID(Cursor c, int ID) {
		int pos = -1;
		if (!c.moveToFirst())
			return -1;
		do {
			pos++;
			if (c.getInt(0) == ID) {
				break;
			}

		} while (c.moveToNext());

		return pos;
	}

	// 播放指定 ID 的歌曲
	private void playAtID(int ID) {
		// TODO Auto-generated method stub
		currentMusicIndex = getMusicPositionByID(mCursor, ID);
		playMusic();
	}

	class MyPhoneStateListener extends PhoneStateListener {

		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // 由通话状态挂断
				if (MpStatus == SMPConstants.STATUS_CALLIN_PAUSE)
					continueMusic();
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK: // 摘机
				if (mp.isPlaying()) {
					pauseMuisc();
					MpStatus = SMPConstants.STATUS_CALLIN_PAUSE;
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING: // 来电
				if (mp.isPlaying()) {
					pauseMuisc();
					MpStatus = SMPConstants.STATUS_CALLIN_PAUSE;
				}
				break;
			default:
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}

}
