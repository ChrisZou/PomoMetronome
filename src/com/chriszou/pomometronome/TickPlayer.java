package com.chriszou.pomometronome;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class TickPlayer {
	AudioTrack mAudioTrack;
	private int mResId;
	private Context mContext;
	private final PlayThread mPlayThread;
	private final PlayHandler mPlayHandler;
	private byte[] mData;

	public TickPlayer(Context context, int resId) {
		mContext = context;
		mResId = resId;

		mPlayThread = new PlayThread("play");
		mPlayThread.start();
		mPlayHandler = new PlayHandler(mPlayThread.getLooper());
	}

	public void setPCMInfo(int sample, int chanel, int audioFormat) {
		int minBufferSize = AudioTrack.getMinBufferSize(sample, chanel, audioFormat);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample, chanel, audioFormat, minBufferSize, AudioTrack.MODE_STREAM);

		readData();
	}

	private void readData() {
		try {
			int i = 0;
			byte[] buffer = new byte[512];
			InputStream inputStream = mContext.getResources().openRawResource(mResId);
			// Skip header
			inputStream.skip(44);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
			while ((i = inputStream.read(buffer)) != -1) {
				bos.write(buffer, 0, i);
			}
			bos.flush();
			mData = bos.toByteArray();
			inputStream.close();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int mInterval = 80;
	public void setRate(int rate) {
		mInterval = (int) (60.0f / (float) rate * 1000.0f);
	}

	private volatile boolean mRunning = false;

	public void start() {
		if (!mRunning) {
			mRunning = true;
			mPlayHandler.loop();
		}
	}

	private void play() {
		mAudioTrack.play();
		mAudioTrack.write(mData, 0, mData.length);
	}

	public void stop() {
		mRunning = false;
	}

	private class PlayHandler extends Handler {
		public PlayHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				loop();
				play();
			}
		}

		public void loop() {
			if (mRunning) {
				sendEmptyMessageDelayed(0, mInterval);
			} else {
				mAudioTrack.flush();
			}
		}
	}

	private class PlayThread extends HandlerThread {

		public PlayThread(String name) {
			super(name);
		}

		@Override
		public void run() {
			super.run();
		}

	}
}
