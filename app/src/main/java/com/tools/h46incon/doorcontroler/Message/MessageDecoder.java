package com.tools.h46incon.doorcontroler.Message;

import com.tools.h46incon.doorcontroler.StreamSplitter.StreamSplitter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;


/**
 * Created by h46incon on 2015/2/23.
 * This class is used to decode message receive from device
 */
public class MessageDecoder {

	private final static StreamSplitter.MsgLenGetterCB msgLenGetter = new StreamSplitter.MsgLenGetterCB() {
		@Override
		public int getLen(byte[] header)
		{
			int len = header[0] & 0xFF;
			len &= ~(1 << 7);
			len <<= 8;
			len |= (header[1] & 0xFF);
			return len;
		}
	};


	public MessageDecoder()
	{
		StreamSplitter.PackageFormat packageFormat = new StreamSplitter.PackageFormat();
		packageFormat.startBytes = Param.startBytes;
		packageFormat.headerLen = Param.headerLen;
		packageFormat.lenGetter = msgLenGetter;

		streamSplitter = new StreamSplitter(packageFormat);
	}

	public List<byte[]> decode(byte[] msg, int length)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(msg, 0, length);
		return decode(byteBuffer);
	}

	public List<byte[]> decode(ByteBuffer byteBuffer)
	{
		List<byte[]> packages = streamSplitter.join(byteBuffer);
		List<byte[]> results = new LinkedList<>();

		for (byte[] pack : packages) {
			ByteBuffer data = getLoad(pack);
			if (data == null) {
				// Note: NACK
			} else {
				// Copy result
				byte[] r = new byte[data.remaining()];
				data.get(r);
				results.add(r);
			}
		}

		return results;
	}

	public void reset()
	{
		streamSplitter.reset();
	}

	private ByteBuffer getLoad(byte[] pack)
	{
		// Check length
		if (pack.length < Param.packageMinLen) {
			return null;
		}

		// Check end bytes
		for (int i = Param.endBytes.length; i > 0; --i) {
			if (pack[pack.length - i] != Param.endBytes[Param.endBytes.length - i]) {
				return null;
			}
		}


		// Check CRC
		CRC32 crc32 = new CRC32();
		final int loadLen = pack.length - Param.headerLen - Param.CRCLen - Param.endBytes.length;
		crc32.update(pack, Param.headerLen, loadLen);
		// Need convert crc32 into int,
		// because I will convert 4bytes data into int later.
		int crcTarget = (int)crc32.getValue();

		// Get CRC value in package
		ByteBuffer byteBuffer = ByteBuffer.wrap(pack, Param.headerLen + loadLen, Param.CRCLen);
		byteBuffer.order(ByteOrder.BIG_ENDIAN);
		int crcExpect = byteBuffer.getInt();

		if (crcTarget != crcExpect) {
			return null;
		}

		return ByteBuffer.wrap(pack, Param.headerLen, loadLen);
	}

	private StreamSplitter streamSplitter;
}
