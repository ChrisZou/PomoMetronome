package com.chriszou.pomometronome;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.chriszou.androidlibs.Toaster;
import com.chriszou.pomometronome.custom.CountDownView;
import com.chriszou.pomometronome.custom.CountDownView.CountDownListener;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

@EActivity
public class PomoActivity extends Activity {

	private static final int POMO_LENGTH_SECONDS = 25 * 60;
	private static final int MAX_RATE = 200;

	@ViewById(R.id.main_pomo_seekbar)
	HoloCircularProgressBar mPomoSeekBar;

	@ViewById(R.id.main_time_left)
	CountDownView mCountDownView;

	@ViewById(R.id.main_rate)
	TextView mRateView;
	
	@ViewById(R.id.main_rate_seekbar)
	SeekBar mRateSeekBar;
	
	@ViewById(R.id.main_start)
	Button mStartButton;

	private int mNomeRate = 80;
	private TickPlayer mTickPlayer;

	@Pref
	MyPrefs_ mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pomo_main);

		// Keep the screen on in this activity
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mTickPlayer = new TickPlayer(this, R.raw.metronome);
		mTickPlayer.setPCMInfo(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
	}

	@AfterViews
	void initViews() {
		mPomoSeekBar.setMarkerProgress(1.0f);
		mPomoSeekBar.setProgressBackgroundColor(Color.GREEN);
		mPomoSeekBar.setProgressColor(Color.RED);
		updateRateView();
		
		mCountDownView.setProgressBar(mPomoSeekBar);

		mRateSeekBar.setMax(MAX_RATE);
		mNomeRate = mPrefs.rate().get();
		mRateSeekBar.setProgress(mNomeRate);
		mRateView.setText(mNomeRate + "");
		mRateSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mNomeRate = progress;
				onRateChanged();
			}
		});
	}

	@Click(R.id.main_decrease)
	void decreaseRate() {
		mNomeRate--;
		onRateChanged();
	}

	@Click(R.id.main_increase)
	void increaseRate() {
		mNomeRate++;
		onRateChanged();
	}

	private void onRateChanged() {
		mPrefs.rate().put(mNomeRate);
		updateRateView();
		if (mTickPlayer != null) {
			mTickPlayer.setRate(mNomeRate);
		}
	}

	private void updateRateView() {
		mRateView.setText(mNomeRate + "");
		mRateSeekBar.setProgress(mNomeRate);
	}

	@Click(R.id.main_start)
	void onStartClick() {
		if (mRunning) {
			mRunning = false;
			stop(true);
		} else {
			mRunning = true;
			start();
		}
	}

	void start() {
		mTickPlayer.setRate(mNomeRate);
		mTickPlayer.start();

		mCountDownView.startCountDown(POMO_LENGTH_SECONDS);
		mCountDownView.setCountDownListener(new CountDownListener() {
			@Override
			public void onEnd() {
				stop(false);
			}
		});

		mStartButton.setText("Stop");
	}

	/**
	 * Stop the pomodoro
	 * 
	 * @param fromUser
	 *            Indicate if the event is caused by user clicking "stop" or by
	 *            the end of the CountDownView
	 */
	void stop(boolean fromUser) {
		mRunning = false;
		mStartButton.setText("Start");
		mTickPlayer.stop();
		if (fromUser) {
			mCountDownView.stop();
		}
	}

	private volatile boolean mRunning = false;

	@Override
	public void onBackPressed() {
		if (mRunning) {
			Toaster.s(this, "Please stop the timer first");
			return;
		}
		super.onBackPressed();
	}

}
