package com.tools.h46incon.doorcontroler.StreamSplitter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by h46incon on 2015/1/16.
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

	private static enum ReceivingState{
		START_BYTES,
		LEN_INFO,
		MSG,
		END_BYTES
	}

	private ByteStreamBuffer msgBuf = new ByteStreamBuffer();
	private ReceivingState receivingState = ReceivingState.START_BYTES;
	private int nextPackLen = 0;
	private int msgHeaderLen;
	private MsgLenGetterCB lenGetter;
}
