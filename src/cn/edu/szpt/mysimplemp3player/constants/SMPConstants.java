package cn.edu.szpt.mysimplemp3player.constants;

import android.provider.MediaStore;

public class SMPConstants {
	// MediaPlayer 状态信息
	public static final int STATUS_STOP = 0;
	public static final int STATUS_PLAY = 1;
	public static final int STATUS_PAUSE = 2;
	public static final int STATUS_CALLIN_PAUSE = 3; // 来电暂停
	// Activity 向 Service 传送的命令
	public static final int CMD_PLAY = 1; // 播放
	public static final int CMD_PAUSE = 2; // 暂停
	public static final int CMD_CONTINUE = 3; // 继续播放
	public static final int CMD_PREV = 4; // 上一首
	public static final int CMD_NEXT = 5; // 下一首
	public static final int CMD_GETINFORM = 6; // 获取后台状态信息
	public static final int CMD_CHANDEPROGRESS = 7; // 改变播放进度
	public static final int CMD_PLAYATPOSITION = 8; // 播放指定位置歌曲
	// Service 向 Ativity 发送的请求
	public static final int RETURN_STATUS = 9;
	public static final int CMD_PLAYATID=10;
	// 后台向前台发送的广播
	// 后台向前台发送后台状态信息广播
	public static final String ACT_SERVICE_REQUEST_BROADCAST = "cn.edu.szpt.MyTinyPlayer";
	// 歌曲播放过程中，后台向前台发送歌词信息广播
	public static final String ACT_LRC_RETURN_BROADCAST = "cn.edu.szpt.LrcMessage";
	// 歌曲播放过程中，后台向前台发送播放进度信息广播
	public static final String ACT_PROGRESS_RETURN_BROADCAST = "cn.edu.szpt.progressMessage";
	// 查询 ContentProvider 的相关字段
	public static final String[] mCursorCols = new String[] {
			"audio._id AS _id", // index must match IDCOLIDX below
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION };
}