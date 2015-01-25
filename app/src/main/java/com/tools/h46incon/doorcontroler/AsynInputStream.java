package com.tools.h46incon.doorcontroler;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by h46incon on 2015/1/21.
 * Read data into a buffer
 * Max idle time
 * Callback: idle timeout
 * Callback: enough data read
 */

/*
 *  One thread to read data
 *  One thread to run the callback
 *  If data reading thread has read enough data, then
 *      1) wake up blocked thread waiting for data
 *      2) wake up callback thread to run the callback
 *  Use wait() and notify() to sleep and wake up
 */

public class AsynInputStream {
	public static interface OnReadFinish {
		public void onFinish(byte[] buffer, int off, int len);
	};

	public static interface OnIOException{
		public void onIOException(IOException e);
	}

	public AsynInputStream(InputStream inputStream, OnIOException onIOException)
	{
		this.inputStream = inputStream;
		this.onIOException = onIOException;
	}

	// I will not provide a method to read single byte
	// Because It's not very convince to implement without a array in BlockedInfo
	// And I think it's much more easy for caller to reuse a buffer array
	public int syncRead(byte[] buffer, int off, int len, long timeout)
	{
		BlockedInfo info = new BlockedInfo(buffer, off, len);
		return info.syncWait(timeout);
	}

	public void asynRead(byte[] buffer, int off, int len, OnReadFinish onFinish)
	{

	}

	// get data without blocking, return data number read
	public int getData(byte[] buffer, int startPos, int count)
	{
		return 0;
	}

	// I do need some field outside this class
	private class BlockedInfo {
		public BlockedInfo(byte[] buffer, int off, int expectDataNum)
		{
			this(buffer, off, expectDataNum, null);
		}

		public BlockedInfo(byte[] buffer, int off, int expectDataNum, OnReadFinish readFinsh)
		{
			this.buffer = buffer;
			this.off = off;
			this.expectDataNum = expectDataNum;
			this.onReadFinish = readFinsh;
		}

		// return data has read
		public int syncWait(long timeout)
		{
			try {
				if (blockInfo.offer(this, timeout, TimeUnit.MILLISECONDS)) {
					synchronized (syncObj) {
						syncObj.wait(timeout);
					}
				} else {
					Log.w(TAG, "Can not add block info to queue!");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return readNum;
		}

		public void asyncWait(long timeout)
		{
			// TODO;
		}

		public void runCallBack()
		{
			if (hasRun.compareAndSet(false, true)) {
				if (onReadFinish != null) {
					// Is a async calling, run the callback
					onReadFinish.onFinish(buffer, off, readNum);
				} else {
					// Is a sync calling, need wake up the thread
					synchronized (syncObj) {
						syncObj.notify();
					}
				}
			}
		}

		public boolean hasRun()
		{
			return hasRun.get();
		}

		/*
		 * DO NOT call this function parallel!
		 * @return: If this blocked info take enough data.
		 */
		public boolean offerData(int data)
		{
			if (data == -1) {
				// End of stream.
				Log.w(TAG, "Reach end of stream, this must not happened in bluetooth socket!");
				return true;
			}

			buffer[off + readNum] = (byte) data;
			++readNum;
			return (readNum >= expectDataNum);
			// TODO: Check read complete
		}


		final private Object syncObj = new Object();
		private byte[] buffer;
		private int off;
		private OnReadFinish onReadFinish;   // Null means this is a sync called.
		private int readNum = 0;
		private int expectDataNum;
		private AtomicBoolean hasRun = new AtomicBoolean(false);

	};

	private static class BlockedTimeOutInfo{
		public long timeout;
		public BlockedInfo blockedInfo;
	};

	private Runnable callbackRunner = new Runnable() {
		@Override
		public void run()
		{
			while (isRunning) {
				// TODO: check timeout

				try {
					BlockedInfo info = needWakeInfo.take();
					info.runCallBack();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	};

	// TODO: when to start
	private void startReadThread()
	{
		threadPool.submit(new Runnable() {
			// This runner may not wake up forever
			// Kill it if necessary
			@Override
			public void run()
			{
				int data = -1;
				boolean bufferedData = false;
				while (isRunning) {
					BlockedInfo info = null;
					try {
						info = blockInfo.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (info == null) {
						continue;
					}

					boolean hasReadComplete = false;
					// Offer buffered to this info
					if (bufferedData) {
						hasReadComplete = info.offerData(data);
						bufferedData = false;
					}

					// Start read loop
					while (!hasReadComplete) {
						try {
							data = inputStream.read();
							if (info.hasRun()) {
								// Stop to offer data to this info.
								hasReadComplete = true;
								// Buffered this data
								bufferedData = true;
							} else {
								// offerData() will handler end of stream
								hasReadComplete = info.offerData((byte) data);
							}
						} catch (IOException e) {
							Log.w(TAG, "IO Exception occurs");
							onIOException.onIOException(e);
							Log.d(TAG, "Wake up all block thread and run callback");
							// TODO: handler blocked info
						}
					}

				}
			}
		});
	}

	private final String TAG = "AsynInputStream";
	// TODO: Need different InputStreamReader share a thread pool of multi thread?
	private ExecutorService threadPool = Executors.newSingleThreadExecutor();
	private LinkedBlockingQueue<BlockedInfo> needWakeInfo = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<BlockedInfo> blockInfo = new LinkedBlockingQueue<>();
	private InputStream inputStream;
	private OnIOException onIOException;
	private boolean isRunning = false;
}
