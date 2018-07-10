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

	// ���� MediaPlayer �������Բ�������
	private MediaPlayer mp;
	// ���Լ�¼��������״̬
	private int MpStatus;
	// �α�������Դ�Ų�ѯ�ĸ�����Ϣ
	private Cursor mCursor;
	// ��ǰ���ŵĸ������� Cursor �е����
	private int currentMusicIndex = 0;
	// ����ÿ����ʵ�ʱ�������
	private ArrayList<LrcBean> lrcs;
	// ��һ�������ʾ��ʱ��
	private int nextTimeMil = 0;
	// ��� Arraylist �е����
	private int LrcPos;
	// �������
	private String message;
	// ���Ե��ȸ���߳�
	private Handler lrcHandler = new Handler();
	//
	private lrcCallBack r = null;
	private prgCallBack pr = null;

	// ���Ե��Ƚ����߳�
	private Handler prgHandler = new Handler();

	class prgCallBack implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int time = mp.getCurrentPosition();
			Intent i = new Intent(SMPConstants.ACT_PROGRESS_RETURN_BROADCAST);
			i.putExtra("PROGRESS", time);
			sendBroadcast(i);
			// ÿ�� 300 ���뷢��һ��
			prgHandler.postDelayed(this, 300);
		}

	}

	private void initLrc(String lrcPath) {
		// TODO Auto-generated method stub
		InputStream in;
		try {
			// �ж�ָ���ļ��ı����ʽ
			String charset = LrcProcessor.getCharSet(new FileInputStream(
					lrcPath));
			// ��������ļ�
			LrcProcessor lrcProc = new LrcProcessor();
			in = new FileInputStream(lrcPath);
			lrcs = lrcProc.process(in, charset);
			if (r != null) {
				lrcHandler.removeCallbacks(r);
			}
			// ʵ�����̶߳���
			r = new lrcCallBack(lrcs);
			if (pr != null) {
				prgHandler.removeCallbacks(pr);
			}
			// �����������̶߳���
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

	// ��ȡ�����б�
	private Cursor getMusicList() {
		Uri MUSIC_URL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		return getContentResolver().query(MUSIC_URL, SMPConstants.mCursorCols,
				null, null, null);
	}

	// ����λ������ȡ�����ļ�·��
	public String getDateByPosition(Cursor c, int position) {
		c.moveToPosition(position);
		int dataColumn = c.getColumnIndex(MediaStore.Audio.Media.DATA);
		String data = c.getString(dataColumn);
		return data;
	}

	// ���ݸ������ڵ���ţ���ȡ���Ӧ��_ID ֵ
	private int getMusicIDByPosition(Cursor c, int position) {
		if (c.moveToPosition(position))
			return c.getInt(0);
		else
			return -1;
	}

	// ͨ���㲥��ǰ̨����״̬��Ϣ
	private void sendMPInform() {
		// TODO Auto-generated method stub
		// ���ù㲥 Intent
		Intent i = new Intent(SMPConstants.ACT_SERVICE_REQUEST_BROADCAST);
		i.putExtra("CMD", SMPConstants.RETURN_STATUS);
		// ���͵�״̬��Ϣ
		i.putExtra("MPSTATUS", MpStatus);
		// ��ǰ������_ID ֵ
		i.putExtra("MUSICID", getMusicIDByPosition(mCursor, currentMusicIndex));
		// ���͹㲥
		sendBroadcast(i);
	}

	// ��ͣ����
	private void pauseMuisc() {
		// TODO Auto-generated method stub
		mp.pause();
		lrcHandler.removeCallbacks(r);
		prgHandler.removeCallbacks(pr);
		MpStatus = SMPConstants.STATUS_PAUSE;
		sendMPInform();
	}

	// ��������
	private void continueMusic() {
		// TODO Auto-generated method stub
		mp.start();
		lrcHandler.post(r);
		prgHandler.post(pr);
		MpStatus = SMPConstants.STATUS_PLAY;
		sendMPInform();
	}

	// ����
	private void playMusic() {
		// TODO Auto-generated method stub
		String musicPath = getDateByPosition(mCursor, currentMusicIndex);
		try {
			mp.reset();
			mp.setDataSource(musicPath);
			mp.prepare();
			initLrc(musicPath.substring(0, musicPath.length() - 3) + "lrc");
			// ��������߳�
			lrcHandler.post(r);
			// �����������߳�
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

	// ������һ�ף�����Ѿ��ǵ�һ���򲥷����һ��
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

	// ������һ�ף�����Ѿ������һ���򲥷ŵ�һ��
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
		// ��ȡ�����б���Ϣ���ŵ� mCursor �б���
		mCursor = getMusicList();
		// ��ǰ����λ��ָ���һ�׸���
		currentMusicIndex = 0;
		// ��ǰ������״̬����Ϊ Stop ״̬
		MpStatus = SMPConstants.STATUS_STOP;
		// ʵ���� MediaPlayer ����
		mp = new MediaPlayer();
		// ��������������Զ�������һ��
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				nextMusic();
			}
		});
		// �Ե绰������״̬���м���
		TelephonyManager telManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		// ע��һ���������Ե绰״̬���м���
		telManager.listen(new MyPhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		// ������Ϣ���� intent �е� Extras ��
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
				// ����״ε��ã����ȡ��һ����ʵ���ʾʱ�������
				if (nextTimeMil == 0) {
					nextTimeMil = lrcList.get(LrcPos).getBeginTime();
					message = lrcList.get(LrcPos).getLrcMsg();
				}
				// ��ȡ��ǰ����ʱ��
				int time = mp.getCurrentPosition();
				// ������˸����ʾ��ʱ�䣬�򽫸��ͨ���㲥���ͳ�ȥ
				if (time >= nextTimeMil) {
					// ͨ���㲥����ʷ��͵�ǰ̨
					Intent i = new Intent(SMPConstants.ACT_LRC_RETURN_BROADCAST);
					i.putExtra("LRC", message);
					sendBroadcast(i);
					LrcPos++;
					// ��ȡ������ʵ���ʾʱ��
					nextTimeMil = lrcList.get(LrcPos).getBeginTime();
					// ��ȡ������ʵ�����
					message = lrcList.get(LrcPos).getLrcMsg();
				}
				// ���ʱ��û�г����������ȣ��� 10 ������ٴ����и��߳�
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
		// ���û�п�ʼ���ţ���׼������
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
		// ��ת��ָ����λ��
		mp.seekTo(msec);
		// ���ݸ�����ת��λ�ã���ת����ʶ�Ӧ��λ��
		changeLrcPosition(Long.valueOf(msec));
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ��������
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

	// ���ݸ���_ID ֵ����ȡ���Ӧ�����
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

	// ����ָ�� ID �ĸ���
	private void playAtID(int ID) {
		// TODO Auto-generated method stub
		currentMusicIndex = getMusicPositionByID(mCursor, ID);
		playMusic();
	}

	class MyPhoneStateListener extends PhoneStateListener {

		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // ��ͨ��״̬�Ҷ�
				if (MpStatus == SMPConstants.STATUS_CALLIN_PAUSE)
					continueMusic();
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK: // ժ��
				if (mp.isPlaying()) {
					pauseMuisc();
					MpStatus = SMPConstants.STATUS_CALLIN_PAUSE;
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING: // ����
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
