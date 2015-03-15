package com.tools.h46incon.doorcontroler.Message;

import com.tools.h46incon.doorcontroler.StreamSplitter.SubBytesFinder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

/**
 * Created by h46incon on 2015/2/24.
 * Use to encode message transfer to device
 */
public class MessageEncoder {
	public MessageEncoder()
	{
		outputBuffer = ByteBuffer.allocate(4096);
	}

	public ByteBuffer encode(ByteBuffer input)
	{
		// Prepare
		final int totalLen = getOutputTotalLen(input.remaining());
		if (totalLen > outputBuffer.capacity()) {
			outputBuffer = ByteBuffer.allocate(totalLen);
		}
		outputBuffer.clear();

		// Encode
		if (encode(input, outputBuffer)) {
			outputBuffer.flip();
			return outputBuffer.duplicate();
		} else {
			return null;
		}
	}

	public boolean encode(ByteBuffer input, ByteBuffer output)
	{
		if (output.remaining() < getOutputTotalLen(input.remaining())) {
			throw new IllegalArgumentException("Output buffer is not enough");
		}

		int begPos = output.position();
		// Write random bytes
		if (packData(input, output)) {
			int endPos = output.position();
			// Check if it contain start bytes
			output.position(begPos + 1);
			startBytesFinder.reset();
			if (!startBytesFinder.findIn(output)) {
				output.position(endPos);
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	public int getOutputTotalLen(int inputLen)
	{
		final int totalLen =
				Param.startBytes.length + inputLen + Param.CRCLen + Param.endBytes.length;
		return totalLen;
	}


	private boolean packData(ByteBuffer input, ByteBuffer output)
	{
		final int inputLen = input.remaining();
		// Set endian
		ByteOrder orderBackup = output.order();
		output.order(ByteOrder.BIG_ENDIAN);

		// put start bytes
		output.put(Param.startBytes);

		// Skip header
		final int loadPos = output.position() + Param.headerLen;
		output.position(loadPos);

		// put data
		output.put(input);

		// Put crc32 value
		CRC32 crc32 = new CRC32();
		crc32.update(output.array(), loadPos, inputLen);
		final int crcVal = (int) crc32.getValue();
		output.putInt(crcVal);

		// Put end bytes
		output.put(Param.endBytes);

		// backup end pos
		int endPos = output.position();

		// Put header
		output.position(loadPos - Param.headerLen);
		final int loadLen = inputLen + Param.CRCLen + Param.endBytes.length;
		intToBytes(loadLen, headerBuf);
		output.put(headerBuf);

		// reset some field
		output.order(orderBackup);
		output.position(endPos);

		return true;
	}


	private static void intToBytes(int val, byte[] out)
	{
		for (int i = out.length - 1; i >= 0; --i) {
			out[i] = (byte) (val & 0xFF);
			val >>= 8;
		}
	}

	private SubBytesFinder startBytesFinder = new SubBytesFinder(Param.startBytes);
	private byte[] headerBuf = new byte[Param.headerLen];
	private ByteBuffer outputBuffer;
}
