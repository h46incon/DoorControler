package com.tools.h46incon.doorcontroler.Message;

import android.util.Base64;

import com.tools.h46incon.doorcontroler.StreamSplitter.StreamSplitter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

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


	public MessageDecoder() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException
	{
		StreamSplitter.PackageFormat packageFormat = new StreamSplitter.PackageFormat();
		packageFormat.startBytes = Param.startBytes;
		packageFormat.headerLen = Param.headerLen;
		packageFormat.lenGetter = msgLenGetter;

		streamSplitter = new StreamSplitter(packageFormat);

		cipher = Cipher.getInstance(Param.encryptAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, getRSAPrivateKey());

	}

	private Key getRSAPrivateKey()
	{
		// Decrypt private key
		StringBuilder stringBuilder = new StringBuilder(1024);
		int size = (Param.decodePrivateKey.length() - 4 - 2) / 2;

		stringBuilder.append("MIIC");
		for (int i = 0; i < size; i++) {
			char c = Param.decodePrivateKey.charAt(i * 2 + i % 2 + 4);
			stringBuilder.append(c);
		}
		stringBuilder.append("=");
		stringBuilder.append("=");
		String privateKey = stringBuilder.toString();

		// Gen private key
		byte[] privKeyByte = Base64.decode(privateKey, Base64.DEFAULT);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyByte);

		try {
			KeyFactory keyFactory = KeyFactory.getInstance(Param.keyFactoryAlgorithm);
			return keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return null;
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
				try {
					buffer.clear();
					cipher.doFinal(data, buffer);

					buffer.flip();
					// Skip random byte
					if (buffer.remaining() < Param.randomLenInLoad) {
						return null;
					}
					buffer.position(buffer.position() + Param.randomLenInLoad);

					// copy result
					byte[] r = new byte[buffer.remaining()];
					buffer.get(r);
					results.add(r);
					// Note: ACK
				} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();
				}
			}
		}

		return results;
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
	private ByteBuffer buffer = ByteBuffer.allocate(4096);
	private Cipher cipher;
}
