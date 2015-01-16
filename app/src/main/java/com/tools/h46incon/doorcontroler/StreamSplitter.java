package com.tools.h46incon.doorcontroler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/16.
 */
public class StreamSplitter {

	public static interface MsgLenGetterCB {
		public int getLen(byte[] header);
	}

	public static MsgLenGetterCB getSimpleMsgLenGetterCB(final ByteOrder byteOrder)
	{
		// Note: Maybe a little bit slow, but this function is not used very often
		MsgLenGetterCB simpleMsgLenGetterCB = new MsgLenGetterCB() {
			@Override
			public int getLen(byte[] header)
			{
				final ByteBuffer byteBuffer = ByteBuffer.wrap(header);
				byteBuffer.order(byteOrder);
				return byteBuffer.getInt();
			}
		};

		return simpleMsgLenGetterCB;
	}

	public StreamSplitter(int msgHeaderLen, MsgLenGetterCB lenGetter)
	{
		this.msgHeaderLen = msgHeaderLen;
		this.lenGetter = lenGetter;

	}

	public ArrayList<byte[]> join(byte[] inStream, int length)
	{
		ArrayList<byte[]> msgList = new ArrayList<byte[]>();
		if (length == 0){
			return msgList;
		}
		msgBuf.newInputStream(inStream, length);

		while(true)
		{
			if(nextPackLen == 0) {
				// Get msg bufLen first
				byte[] header = msgBuf.tryGetMsg(msgHeaderLen, false);
				if (header == null) {
					break;
				}
				else{
					// The second byte of header is the length of the package
					nextPackLen = lenGetter.getLen(header) + msgHeaderLen;
				}
			}

			byte[] msg = msgBuf.tryGetMsg(nextPackLen,true);

			if(msg != null){
				msgList.add(msg);
				nextPackLen = 0;
			}
			else {
				break;
			}
		}
		return msgList;
	}

	private class ByteStreamBuf {
		private final int kInitBufLen = 1024;
		byte[] buf = new byte[kInitBufLen];
		int bufLen = 0;

		byte[] inStream = null;
		int inStreamIndex = 0;
		int inStreamLen = 0;

		public ByteStreamBuf() {
		}

		public void newInputStream(byte[] inStream, int length)
		{
			if (inStream != null){
				storeRemainInStream();
			}
			this.inStream = inStream;
			this.inStreamIndex = 0;
			this.inStreamLen = length;
		}

		public byte[] tryGetMsg(int msgLen, boolean needPop)
		{
			if (this.bufLen == 0) {
				// Try Get message from package directly
				if(isEnoughInStream( msgLen)) {
					byte[] msgGetted = new byte[msgLen];
					System.arraycopy(inStream, inStreamIndex, msgGetted, 0, msgLen);
					if (needPop){
						inStreamIndex += msgLen;
					}
					return msgGetted;
				}
				else {
					// Not enough bytes
					storeRemainInStream();
					return null;
				}
			}
			else {
				// try construct message with buffer and inStream
				int lenNeed = msgLen - this.bufLen;
				if (isEnoughInStream(lenNeed)) {
					byte[] msgGetted = new byte[msgLen];
					System.arraycopy(this.buf, 0, msgGetted, 0, this.bufLen);
					System.arraycopy(inStream, inStreamIndex, msgGetted, this.bufLen, lenNeed);

					if (needPop){
						this.bufLen = 0;
						inStreamIndex += lenNeed;
					}

					return msgGetted;
				}
				else {
					storeRemainInStream();
					return null;
				}
			}
		}

		private boolean isEnoughInStream(int len)
		{
			if (inStream == null) {
				return false;
			}
			return (inStreamLen - inStreamIndex) >= len;
		}

		private void storeRemainInStream()
		{
			if (inStream == null) {
				return;
			}
			int lenInStreamRemain = inStreamLen - inStreamIndex;
			int totalLen = lenInStreamRemain + this.bufLen;
			// Need a bigger buffer?
			if (totalLen > this.buf.length)
			{
				byte[] newBuf = new byte[totalLen];
				System.arraycopy(this.buf, 0, newBuf, 0, this.bufLen);
				this.buf = null;
				this.buf = newBuf;
			}

			if (lenInStreamRemain != 0){
				System.arraycopy(inStream, inStreamIndex, this.buf, this.bufLen, lenInStreamRemain);
			}
			this.bufLen = totalLen;
			inStreamIndex = 0;
			inStream = null;
			inStreamLen = 0;
		}

	}
	private ByteStreamBuf msgBuf = new ByteStreamBuf();

	private int nextPackLen = 0;
	private int msgHeaderLen;
	private MsgLenGetterCB lenGetter;
}
