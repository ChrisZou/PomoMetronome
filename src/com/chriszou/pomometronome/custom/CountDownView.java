package com.chriszou.pomometronome.custom;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

public class CountDownView extends TextView {

	private CountDownListener mCountDownListener;
	private long mEndingTime;
	private boolean mRunning;
	private Handler mUiHandler;
	public CountDownView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mUiHandler = new Handler();
	}
	
	public void startCountDown(int seconds) {
		if (!mRunning) {
			mRunning = true;
			mEndingTime = System.currentTimeMillis() + seconds * 1000;
		}
	}
	
	public void stop() {
		if (mRunning) {
			mRunning = false;
			mUiHandler.removeCallbacks(mUpdateTast);
			setText("00:00");
		}
	}

	private void loop() {
		mUiHandler.postDelayed(mUpdateTast, 1000);
	}

	public boolean isRunning() {
		return mRunning;
	}
	
	public void setCountDownListener(CountDownListener l) {
		mCountDownListener = l;
	}

	public static interface CountDownListener {
		public void onEnd();
	}

	private Runnable mUpdateTast = new Runnable() {
		@Override
		public void run() {
			long now = System.currentTimeMillis();
			long remain = mEndingTime - now;
			int remainingSecond = (int) remain / 1000;
			int min = remainingSecond / 60;
			int sec = remainingSecond % 60;

			String time = String.format("%02d:%02d", min, sec);
			setText(time);

			if (remain >= 1000) {
				loop();
			} else {
				mRunning = false;
				if (mCountDownListener != null) {
					mCountDownListener.onEnd();
				}
			}
		}
	};
}
