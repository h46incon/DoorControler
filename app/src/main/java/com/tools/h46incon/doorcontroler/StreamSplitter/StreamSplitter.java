package com.tools.h46incon.doorcontroler.StreamSplitter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by h46incon on 2015/1/16.
 * Split stream into package
 * The attributes of package is store in StreamSplitter.PackageFormat,
 * contain start bytes, header, message
 * The result package contain header and message body, but not start bytes.
 */
public class StreamSplitter {
	// A callback when need get package's length from header
	// LENGTH means loaded data's length, NOT including header
	public static interface MsgLenGetterCB {
		public int getLen(byte[] header);
	}

	public static class PackageFormat{
		public byte[] startBytes;
		public int headerLen;
		public MsgLenGetterCB lenGetter;
	}


	public static MsgLenGetterCB simpleMsgLenGetterCB(final ByteOrder byteOrder)
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


	public StreamSplitter(PackageFormat packageFormat)
	{
		this.packageFormat = packageFormat;
		this.msgHeaderLen = packageFormat.headerLen;
		this.lenGetter = packageFormat.lenGetter;

		startBytesFinder = new SubBytesFinder(packageFormat.startBytes);
		receivingState = ReceivingState.START_BYTES;
	}

	public List<byte[]> join(byte[] inStream, int length)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(inStream, 0, length);
		return this.join(byteBuffer);
	}

	private List<byte[]> join(ByteBuffer inStream)
	{
		List<ByteBuffer> buffers = startBytesFinder.slip(inStream);
		List<byte[]> result = new LinkedList<>();

		for (ByteBuffer buf : buffers) {
			byte[] pack = joinWithBufferPiece(buf);
			if (pack != null) {
				result.add(pack);
			}
		}

		return result;
	}

	private byte[] joinWithBufferPiece(ByteBuffer inStream)
	{
		if (inStream == null) {
			// is a start byte
			this.msgBuf.Clear();
			// enter next state of START_BYTES
			this.receivingState = ReceivingState.START_BYTES;
			enterNextReceivingState();
			return null;
		} else {
			// normal data
			if (receivingState == ReceivingState.START_BYTES) {
				return null;
			} else {
				return joinNormalData(inStream);
			}
		}
	}

	// Join normal data (expect start bytes)
	// When current state is not waiting for start bytes
	private byte[] joinNormalData(ByteBuffer inStream)
	{
		msgBuf.newInputStream(inStream);
		if (receivingState == ReceivingState.HEADER) {
			// do not pop header from buffer
			byte[] header = msgBuf.tryGetMsg(msgHeaderLen, false);
			if (header == null) {
				return null;
			}
			else {
				// package's length include header
				nextPackLen = lenGetter.getLen(header) + msgHeaderLen;
				enterNextReceivingState();
			}
		}

		if (receivingState == ReceivingState.MSG) {
			byte[] msg = msgBuf.tryGetMsg(nextPackLen,true);
			if (msg != null) {
				enterNextReceivingState();
			}
			return msg;
		}
		return null;
	}

	private void enterNextReceivingState()
	{
		int index = receivingState.ordinal();
		ReceivingState[] states = ReceivingState.values();
		++index;
		if (index == states.length) {
			index = 0;
		}
		receivingState = states[index];
	}

	private static enum ReceivingState{
		START_BYTES,
		HEADER,
		MSG,
	}

	private ByteStreamBuffer msgBuf = new ByteStreamBuffer();
	private ReceivingState receivingState;
	private int nextPackLen = 0;
	final private int msgHeaderLen;
	final private MsgLenGetterCB lenGetter;

	final private PackageFormat packageFormat;
	final private SubBytesFinder startBytesFinder;
}
