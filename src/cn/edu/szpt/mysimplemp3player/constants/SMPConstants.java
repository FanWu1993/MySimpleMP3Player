package cn.edu.szpt.mysimplemp3player.constants;

import android.provider.MediaStore;

public class SMPConstants {
	// MediaPlayer ״̬��Ϣ
	public static final int STATUS_STOP = 0;
	public static final int STATUS_PLAY = 1;
	public static final int STATUS_PAUSE = 2;
	public static final int STATUS_CALLIN_PAUSE = 3; // ������ͣ
	// Activity �� Service ���͵�����
	public static final int CMD_PLAY = 1; // ����
	public static final int CMD_PAUSE = 2; // ��ͣ
	public static final int CMD_CONTINUE = 3; // ��������
	public static final int CMD_PREV = 4; // ��һ��
	public static final int CMD_NEXT = 5; // ��һ��
	public static final int CMD_GETINFORM = 6; // ��ȡ��̨״̬��Ϣ
	public static final int CMD_CHANDEPROGRESS = 7; // �ı䲥�Ž���
	public static final int CMD_PLAYATPOSITION = 8; // ����ָ��λ�ø���
	// Service �� Ativity ���͵�����
	public static final int RETURN_STATUS = 9;
	public static final int CMD_PLAYATID=10;
	// ��̨��ǰ̨���͵Ĺ㲥
	// ��̨��ǰ̨���ͺ�̨״̬��Ϣ�㲥
	public static final String ACT_SERVICE_REQUEST_BROADCAST = "cn.edu.szpt.MyTinyPlayer";
	// �������Ź����У���̨��ǰ̨���͸����Ϣ�㲥
	public static final String ACT_LRC_RETURN_BROADCAST = "cn.edu.szpt.LrcMessage";
	// �������Ź����У���̨��ǰ̨���Ͳ��Ž�����Ϣ�㲥
	public static final String ACT_PROGRESS_RETURN_BROADCAST = "cn.edu.szpt.progressMessage";
	// ��ѯ ContentProvider ������ֶ�
	public static final String[] mCursorCols = new String[] {
			"audio._id AS _id", // index must match IDCOLIDX below
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION };
}