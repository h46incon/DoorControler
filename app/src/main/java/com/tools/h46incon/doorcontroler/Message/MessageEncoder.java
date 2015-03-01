package com.tools.h46incon.doorcontroler.Message;

import com.tools.h46incon.doorcontroler.StreamSplitter.SubBytesFinder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import javax.crypto.NoSuchPaddingException;

/**
 * Created by h46incon on 2015/2/24.
 * Use to encode message transfer to device
 */
public class MessageEncoder {
	public MessageEncoder() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException
	{
		outputBuffer = ByteBuffer.allocate(4096);
		inputBuffer = ByteBuffer.allocate(4096);

		outputBuffer.order(ByteOrder.BIG_ENDIAN);
	}

	public ByteBuffer encode(ByteBuffer message)
	{
		// Write random bytes
		if (packData(message)) {
			// Check if it contain start bytes
			outputBuffer.position(1);
			startBytesFinder.reset();
			if (!startBytesFinder.findIn(outputBuffer)) {
				outputBuffer.position(0);
				return outputBuffer;
			} else {
				return null;
			}
		}

		return null;
	}

	private boolean packData(ByteBuffer input)
	{
		// Prepare
		final int inputLen = input.remaining();
		final int totalLen =
				Param.startBytes.length + inputLen + Param.CRCLen + Param.endBytes.length;
		if (totalLen > outputBuffer.capacity()) {
			outputBuffer = ByteBuffer.allocate(totalLen);
		}
		outputBuffer.clear();

		// put start bytes
		outputBuffer.put(Param.startBytes);

		// Skip header
		final int loadPos = outputBuffer.position() + Param.headerLen;
		outputBuffer.position(loadPos);

		// put data
		outputBuffer.put(input);

		// Put crc32 value
		CRC32 crc32 = new CRC32();
		crc32.update(outputBuffer.array(), loadPos, inputLen);
		final int crcVal = (int) crc32.getValue();
		outputBuffer.putInt(crcVal);

		// Put end bytes
		outputBuffer.put(Param.endBytes);

		// Flip
		outputBuffer.flip();

		// Put header
		outputBuffer.position(Param.startBytes.length);
		final int loadLen = inputLen + Param.CRCLen + Param.endBytes.length;
		intToBytes(loadLen, headerBytes);
		outputBuffer.put(headerBytes);

		outputBuffer.position(0);
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
	private byte[] headerBytes = new byte[Param.headerLen];
	private ByteBuffer outputBuffer;
	private ByteBuffer inputBuffer;
}
