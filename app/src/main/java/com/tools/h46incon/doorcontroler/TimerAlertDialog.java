package com.tools.h46incon.doorcontroler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

/**
 * Created by h46incon on 2015/7/16.
 */
public class TimerAlertDialog{
	public static interface ConfirmButtonTextMaker {
		String onMake(int time_remain_sec);
	}

	public TimerAlertDialog(AlertDialog mDialog, int timeout_s, ConfirmButtonTextMaker textMaker)
	{
		this.mDialog = mDialog;
		this.timeout_s = timeout_s;
		this.confirmButtonTextMaker = textMaker;
	}

	public void cancelTimer()
	{
		if (runningTimmer != null) {
			try {
				runningTimmer.cancel();
			} catch (NullPointerException e) {

			}

			runningTimmer = null;
		}
	}

	public void show()
	{
		mDialog.show();

		final Button diaConfirmButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);

		// Init text
		final String text = confirmButtonTextMaker.onMake(timeout_s);
		diaConfirmButton.setText(text);

		final long endWaitTime = System.currentTimeMillis() + waitDiaShowTime;

		while (!mDialog.isShowing()
				&& System.currentTimeMillis() < endWaitTime) {
			;
		}


		CountDownTimer countDownTimer =
				makeCountDownTimer(diaConfirmButton);

		if (this.runningTimmer != null) {
			this.runningTimmer.cancel();
			Log.w(TAG, "Last timmer may not been canceled!");
		}
		this.runningTimmer = countDownTimer;
		countDownTimer.start();

	}

	private CountDownTimer makeCountDownTimer(final Button diaConfirmButton)
	{
		final int msInS = 1000;
		// Add 100ms to timeout to make it show the "0" time
		return new CountDownTimer(timeout_s * msInS + 100, msInS) {
			int counter = 0;

			@Override
			public void onTick(long millisUntilFinished)
			{
				if (mDialog.isShowing()) {
					++counter;
					final String text = confirmButtonTextMaker.onMake(timeout_s - counter);
					mHandler.post(new Runnable() {
						@Override
						public void run()
						{
							diaConfirmButton.setText(text);
						}
					});
				} else {
					Log.d(TAG, "dialog not shown, timer canceled");
					this.cancel();
					unsetRunningTimer();
				}
			}

			@Override
			public void onFinish()
			{
				if (mDialog.isShowing()) {
					final String text = confirmButtonTextMaker.onMake(timeout_s - counter);
					Log.d(TAG, "time out, trigger button");
					diaConfirmButton.callOnClick();
				}
				unsetRunningTimer();
			}

			private void unsetRunningTimer()
			{
				if (runningTimmer == this) {
					runningTimmer = null;
				}
			}
		};
	}

	private static final String TAG = "TimerAlertDialog";
	private static final long waitDiaShowTime = 1000;

	private CountDownTimer runningTimmer;
	private AlertDialog mDialog;
	private ConfirmButtonTextMaker confirmButtonTextMaker;
	private int timeout_s;
	private Handler mHandler = new Handler();

}

