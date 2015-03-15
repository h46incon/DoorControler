package com.tools.h46incon.doorcontroler.StreamSplitter;

import java.nio.ByteBuffer;

/**
 * Created by h46incon on 2015/2/17.
 * It could get bytes with given length, but may stored in different input stream
 */
class ByteStreamBuffer {
	private static final int kDefaultInitBufLen = 1024;
	private ByteBuffer newInBuffer;
	private ByteBuffer remainBuffer;

	public ByteStreamBuffer()
	{
		this(kDefaultInitBufLen);
	}

	public ByteStreamBuffer(int initBufLen) {
		remainBuffer = ByteBuffer.allocate(initBufLen);
		resetBuffer(remainBuffer);
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

		// TODO: new too many big object may lead performance problem
		byte[] msgReturn = new byte[msgLen];
		final int remainBufPopLen =
				Math.min(remainBuffer.remaining(), msgLen);
		if (remainBufPopLen > 0) {
			getDataFromBuffer(remainBuffer, msgReturn, 0, remainBufPopLen, needPop);
		}

		final int newBufPopLen = msgLen - remainBufPopLen;
		if (newBufPopLen > 0) {
			getDataFromBuffer(newInBuffer, msgReturn, remainBufPopLen, newBufPopLen, needPop);
		}

		return msgReturn;

	}

	public void clear(){
		resetBuffer(remainBuffer);
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

	public void storeRemainInStream()
	{
		if (newInBuffer == null || !newInBuffer.hasRemaining()) {
			return;
		}
		// Clean remainBuffer, to make most usage of it
		if (!remainBuffer.hasRemaining()){
			resetBuffer(remainBuffer);
		}
		int totalLen = newInBuffer.remaining() + remainBuffer.remaining();

		// Enable remainBuffer to WRITE
		int lastRemainBufPos = remainBuffer.position();
		remainBuffer.position(remainBuffer.limit());
		remainBuffer.limit(remainBuffer.capacity());

		// when could not append directly
		if (newInBuffer.remaining() > remainBuffer.remaining()) {
			if (remainBuffer.capacity() >= totalLen) {
				// Just compact
				remainBuffer.compact();
				lastRemainBufPos = 0;
			} else {
				// need new a buffer
				ByteBuffer newBuffer = ByteBuffer.allocate(totalLen);
				// Make remainBuffer to READ
				remainBuffer.limit(remainBuffer.position());
				remainBuffer.position(lastRemainBufPos);

				// Move to new buffer
				newBuffer.put(remainBuffer);
				remainBuffer = newBuffer;

				// reset pos to zero
				lastRemainBufPos = 0;
			}

		}
		// append remain data
		remainBuffer.put(newInBuffer);

		// Make remainBuffer to Read
		// Do not use flip() function, because position may not be 0 after flip
		remainBuffer.limit(remainBuffer.position());
		remainBuffer.position(lastRemainBufPos);

		newInBuffer = null;
	}

	// clear buffer, and make it into read state
	private static void resetBuffer(ByteBuffer buffer)
	{
		buffer.clear();
		// Now buffer is read for write
		// Flip to read
		buffer.flip();
	}

}

