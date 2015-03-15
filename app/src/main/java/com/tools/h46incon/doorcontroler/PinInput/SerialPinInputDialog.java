package com.tools.h46incon.doorcontroler.PinInput;

import android.app.FragmentManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Administrator on 2015/3/14.
 */
public class SerialPinInputDialog {
	public interface OnWorkFinished{
		public void onFinished(List<char[]> result, boolean hasAllFinished);
	}

	public SerialPinInputDialog addDialog(CharSequence title, CharSequence message, String TAG)
	{
		DialogInfo info = new DialogInfo();
		info.title = title;
		info.message = message;
		info.TAG = TAG;
		dialogInfos.add(info);
		return this;
	}

	public SerialPinInputDialog setOnWorkFinishedListener(OnWorkFinished onWorkFinishedListener)
	{
		this.onWorkFinishedListener = onWorkFinishedListener;
		return this;
	}


	public void startWork(final FragmentManager fragmentManager)
	{
		result.clear();
		showEachDialog(fragmentManager);
	}


	// This is a recursive function
	private void showEachDialog(final FragmentManager fragmentManager)
	{
		if (dialogInfos.isEmpty()) {
			runWorkFinishedListener(true);
			return;
		}

		DialogInfo info = dialogInfos.poll();
		PinInputDialog inputDialog = new PinInputDialog();
		inputDialog
				.setTitle(info.title)
				.setMessage(info.message)
				.setOnPinInputFinish(new PinInputDialog.OnPinInputFinish() {
					@Override
					public void onFinish(boolean isSuccess, char[] input)
					{
						if (isSuccess) {
							result.add(input);
							// show next dialog
							showEachDialog(fragmentManager);
						} else {
							dialogInfos.clear();
							runWorkFinishedListener(false);
						}
					}
				})
				.show(fragmentManager, info.TAG);

	}

	private void runWorkFinishedListener(boolean hasAllFinished)
	{
		if (onWorkFinishedListener != null) {
			onWorkFinishedListener.onFinished(result, hasAllFinished);
		} else {
			Log.w(TAG, "call back function not set!");
		}
	}


	private static final String TAG = "SerialPinInput";
	private List<char[]> result = new LinkedList<>();

	private class DialogInfo{
		public CharSequence title;
		public CharSequence message;
		public String TAG;
	}

	private Queue<DialogInfo> dialogInfos = new LinkedList<>();
	private OnWorkFinished onWorkFinishedListener;
}
