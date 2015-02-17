package com.tools.h46incon.doorcontroler.StreamSplitter;

import java.nio.ByteBuffer;

/**
 * Created by h46incon on 2015/2/17.
 * It could get bytes with given length, but may stored in different input stream
 */
class ByteStreamBuffer {
	private final int kInitBufLen = 1024;
	private ByteBuffer newInBuffer;
	private ByteBuffer remainBuffer;

	public ByteStreamBuffer() {
		// Flip remainBuffer to read
		remainBuffer = ByteBuffer.allocate(kInitBufLen);
		remainBuffer.flip();
	}

	public void newInputStream(byte[] inStream, int length)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(inStream, 0, length);
		this.newInputStream(byteBuffer);
	}

	public void newInputStream(ByteBuffer newInBuffer)
	{
		if (this.newInBuffer != null){
			storeRemainInStream();
		}
		this.newInBuffer = newInBuffer;
	}

	public byte[] tryGetMsg(int msgLen, boolean needPop)
	{
		int dataRemain = remainBuffer.remaining();
		if (newInBuffer != null) {
			dataRemain += newInBuffer.remaining();
		}
		// test if data is enough
		if (dataRemain < msgLen) {
			// Not enough bytes
			storeRemainInStream();
			return null;
		}

		byte[] msgReturn = new byte[msgLen];
		final int remainBufPopLen =
				(remainBuffer.remaining() >= msgLen) ? remainBuffer.remaining() : msgLen;
		getDataFromBuffer(remainBuffer, msgReturn, 0, remainBufPopLen, needPop);

		final int newBufPopLen = msgLen - remainBufPopLen;
		if (newBufPopLen > 0) {
			getDataFromBuffer(newInBuffer, msgReturn, remainBufPopLen, newBufPopLen, needPop);
		}

		return msgReturn;

	}

	public void Clear(){
		remainBuffer.clear();
		newInBuffer = null;
	}

	private static void getDataFromBuffer(
			ByteBuffer buf, byte[] dst, int offset, int len, boolean needPop)
	{
		if (needPop) {
			buf.get(dst, offset, len);
		} else {
			buf.mark();
			buf.get(dst, offset, len);
			buf.reset();
		}
	}

	private void storeRemainInStream()
	{
		if (newInBuffer == null || !newInBuffer.hasRemaining()) {
			return;
		}
		// Clean remainBuffer, to make most usage of it
		if (!remainBuffer.hasRemaining()){
			remainBuffer.clear();
		}
		int totalLen = newInBuffer.remaining() + remainBuffer.remaining();

		// flip remainBuffer to WRITE
		remainBuffer.flip();

		// when could not append directly
		if (newInBuffer.remaining() > remainBuffer.remaining()) {
			if (remainBuffer.capacity() >= totalLen) {
				// Just compact
				remainBuffer.compact();
			} else {
				// need new a buffer
				ByteBuffer newBuffer = ByteBuffer.allocate(totalLen);
				// flip remainBuffer to READ
				remainBuffer.flip();
				newBuffer.put(remainBuffer);
				remainBuffer.clear();
				remainBuffer = newBuffer;
			}

		}
		// append remain data
		remainBuffer.put(newInBuffer);

		// flip remainBuffer to Read
		remainBuffer.flip();

		newInBuffer = null;
	}

}

