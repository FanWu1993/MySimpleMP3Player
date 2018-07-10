package cn.edu.szpt.mysimplemp3player.lrc;

public class LrcBean implements Comparable<LrcBean> {
	// ��ʿ�ʼʱ��
	private int beginTime;
	// �����Ϣ
	private String lrcMsg;

	public LrcBean(int beginTime, String lrcMsg) {
		super();
		this.beginTime = beginTime;
		this.lrcMsg = lrcMsg;
	}

	public int getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(int beginTime) {
		this.beginTime = beginTime;
	}

	public String getLrcMsg() {
		return lrcMsg;
	}

	public void setLrcMsg(String lrcMsg) {
		this.lrcMsg = lrcMsg;
	}

	@Override
	public int compareTo(LrcBean another) {
		// TODO Auto-generated method stub
		return this.beginTime - another.beginTime;
	}
}