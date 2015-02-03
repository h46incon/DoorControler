package com.tools.h46incon.doorcontroler.BGWorker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import com.tools.h46incon.doorcontroler.MyApp;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by h46incon on 2015/2/3.
 * A background worker showing a progress dialog
 */
public class BGWorker {

	public static enum WorkState{
		SUCCESS,
		EXCEPTION,
		CANCEL,
		TIME_OUT
	}

	public static interface OnWorkFinished{
		public void onWorkFinished(WorkState state, Object result);
	}


	public BGWorker(Context actContext, final Callable task, final OnWorkFinished onWorkFinished)
	{
		this.task = task;
		this.onWorkFinished = onWorkFinished;

		this.progressDialog = new ProgressDialog(actContext);
		initProgressDialog();
	}

	private void initProgressDialog()
	{
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);

		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog)
			{
				CancelTask(WorkState.CANCEL);
			}
		});
	}

	public void start(long timeOutMS)
	{
		this.taskFuture = singleThreadPool.submit(taskRunner);

		// timeout event if failed
		mainHandler.postDelayed(
				new Runnable() {
					@Override
					public void run()
					{
						CancelTask(WorkState.TIME_OUT);
					}
				}, timeOutMS
		);

		progressDialog.show();
	}

	public BGWorker setTitle(CharSequence title)
	{
		progressDialog.setTitle(title);
		return this;
	}

	public BGWorker setMessage(CharSequence message)
	{
		progressDialog.setMessage(message);
		return this;
	}

	private void CancelTask(WorkState state)
	{
		if (isCallbackRunned.compareAndSet(false, true)) {
			// Cancel it and run callback
			Log.d(TAG, "Canceling task...");
			taskFuture.cancel(true);
			onWorkFinished.onWorkFinished(state, null);
		}
		progressDialog.dismiss();
	}

	private final Runnable taskRunner =
			new Runnable() {
				@Override
				public void run()
				{

					WorkState state;
					Object result = null;

					try {
						result = task.call();
						state = WorkState.SUCCESS;
					} catch (Exception e) {
						state = WorkState.EXCEPTION;
						e.printStackTrace();
						Log.d(TAG, "Ignored exception in background task");
					}

					progressDialog.dismiss();

					// if complete
					if (isCallbackRunned.compareAndSet(false, true)) {
						onWorkFinished.onWorkFinished(state, result);
					}
				}
			};

	private final String TAG = "BGWorker";
	private final ProgressDialog progressDialog;
	private final Callable task;
	private final OnWorkFinished onWorkFinished;
	private Future<?> taskFuture;
	private final AtomicBoolean isCallbackRunned = new AtomicBoolean(false);

	private static Handler mainHandler = new Handler(MyApp.getContext().getMainLooper());
	private static ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
}
