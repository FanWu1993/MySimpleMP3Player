package cn.edu.szpt.mysimplemp3player.lrc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class LrcProcessor {
	ArrayList<LrcBean> lrcmap = new ArrayList<LrcBean>();

	// �жϸ���ļ��ı���
	public static String getCharSet(InputStream in) {
		byte[] b = new byte[3];
		String charset = "";
		try {
			in.read(b);
			in.close();
			if (b[0] == (byte) 0xEF && b[1] == (byte) 0xBB
					&& b[2] == (byte) 0xBF)
				charset = "UTF-8";
			else if (b[0] == (byte) 0xFE && b[1] == (byte) 0xFF)
				charset = "UTF-16BE";
			else if (b[0] == (byte) 0xFF && b[1] == (byte) 0xFE)
				charset = "UTF-16LE";
			else
				charset = "GBK";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return charset;

	}

	// ��������ļ���������Ľ���� ArrayList ����ʽ����
	public ArrayList<LrcBean> process(InputStream in, String charset) {
		try {
			InputStreamReader inreader;
			if (!charset.equals("")) {
				inreader = new InputStreamReader(in, charset);
			} else {
				inreader = new InputStreamReader(in);
			}
			BufferedReader br = new BufferedReader(inreader);
			String temp;
			while ((temp = br.readLine()) != null) {
				paraseLine(temp);
			}
			Collections.sort(lrcmap);
			br.close();
			in.close();
		} catch (IOException ex) {
			Log.i("IOException", ex.getMessage());
		}
		return lrcmap;
	}

	// ���ո���ļ��ĸ�ʽ������һ������
	private void paraseLine(String str) {
		String msg;
		// ȡ�ø�������Ϣ
		if (str.startsWith("[ti:")) {
			String title = str.substring(4, str.length() - 1);
			System.out.println("title--->" + title);
		}// ȡ�ø�����Ϣ
		else if (str.startsWith("[ar:")) {
			String singer = str.substring(4, str.length() - 1);
			System.out.println("singer--->" + singer);
		}// ȡ��ר����Ϣ
		else if (str.startsWith("[al:")) {
			String album = str.substring(4, str.length() - 1);
			System.out.println("album--->" + album);
		}// ͨ������ȡ��ÿ������Ϣ
		else {
			Pattern p = Pattern
					.compile("\\[\\s*[0-9]{1,2}\\s*:\\s*[0-5][0-9]\\s*[\\.:]?\\s*[0-9]?[0-9]?\\s*\\]");
			Matcher m = p.matcher(str);
			msg = str.substring(str.lastIndexOf("]") + 1);
			while (m.find()) {
				String timestr = m.group();
				timestr = timestr.substring(1, timestr.length() - 1);
				int timeMil = time2long(timestr);
				LrcBean temp = new LrcBean(timeMil, msg);
				lrcmap.add(temp);

				Log.i("Test", timeMil + "---" + msg);
			}
		}
	}

	// �� mm��ss.ddd ��ʽ��ʱ��ת��Ϊ����ֵ
	private int time2long(String timestr) {
		int min = 0, sec = 0, mil = 0;
		try {
			timestr = timestr.replace(".", ":");
			String[] s = timestr.split(":");
			switch (s.length) {
			case 2:
				min = Integer.parseInt(s[0]);
				sec = Integer.parseInt(s[1]);
				break;
			case 3:
				min = Integer.parseInt(s[0]);
				sec = Integer.parseInt(s[1]);
				mil = Integer.parseInt(s[2]);
				break;
			default:
				break;
			}
		} catch (Exception ex) {
			Log.i("LrcErr", timestr + ex.getMessage());
		}
		return min * 60 * 1000 + sec * 1000 + mil * 10;
	}
}