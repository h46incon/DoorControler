package com.tools.h46incon.doorcontroler.BGWorker;

import android.content.Context;
import android.os.Handler;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 * Created by h46incon on 2015/2/3.
 * Run serial task in background with a progress dialog
 */
public class SerialBGWorker {
	private Context actContext;

	public static interface OnPerWorkFinished{
		public boolean onPerWorkFinished(BGWorker.WorkState state, Object reslut);

	}

	public static class taskInfo{
		public Callable task;
		public OnPerWorkFinished onPerWorkFinished;
		public CharSequence title = null;
		public CharSequence message = null;
		public long timeout;
	}

	public SerialBGWorker(Context actContext)
	{
		this.actContext = actContext;
		this.bgThreadHandler = new Handler(actContext.getMainLooper());
	}

	public void addTask(taskInfo info)
	{
		taskInfos.addLast(info);
	}

	public void addTaskInFirst(taskInfo info)
	{
		taskInfos.addFirst(info);
	}

	public boolean removeTask(taskInfo info)
	{
		return taskInfos.remove(info);
	}

	public void start()
	{
		runNextTask();
	}

	private void runNextTask()
	{
		if (!taskInfos.isEmpty()) {
			final taskInfo info = taskInfos.poll();
			BGWorker bgWorker = new BGWorker(actContext, info.task,
					new BGWorker.OnWorkFinished() {
						@Override
						public void onWorkFinished(BGWorker.WorkState state, Object result)
						{
							boolean needContinue =
									info.onPerWorkFinished.onPerWorkFinished(state, result);
							if (needContinue) {
								bgThreadHandler.post(nextTaskRunner);
							}
						}
					});

			bgWorker.setMessage(info.message).setTitle(info.title);
			bgWorker.start(info.timeout);

		}
	}

	private Runnable nextTaskRunner = new Runnable() {
		@Override
		public void run()
		{
			runNextTask();
		}
	};

	private Deque<taskInfo> taskInfos = new LinkedList<>();
	private Handler bgThreadHandler;
}
