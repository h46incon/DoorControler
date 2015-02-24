package com.tools.h46incon.doorcontroler.Message;

import android.util.Base64;

import com.tools.h46incon.doorcontroler.StreamSplitter.SubBytesFinder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.zip.CRC32;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

/**
 * Created by h46incon on 2015/2/24.
 * Use to encode message transfer to device
 */
public class MessageEncoder {
	public MessageEncoder() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException
	{
		cipher = Cipher.getInstance(Param.encryptAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
		outputBuffer = ByteBuffer.allocate(4096);
		inputBuffer = ByteBuffer.allocate(4096);

		outputBuffer.order(ByteOrder.BIG_ENDIAN);
	}

	public ByteBuffer encode(ByteBuffer message)
	{
		// Copy data
		copyInputInBuffer(message);

		int maxTryTimes = 5;
		for (int i = 0; i < maxTryTimes; i++) {
			// Write random bytes
			writeRandomInInputBuf();
			inputBuffer.position(0);

			if (packData()) {
				// Check if it contain start bytes
				outputBuffer.position(1);
				startBytesFinder.reset();
				if (!startBytesFinder.findIn(outputBuffer)) {
					outputBuffer.position(0);
					return outputBuffer;
				}
			}
		}

		return null;
	}

	private boolean packData()
	{
		outputBuffer.clear();

		// put start bytes
		outputBuffer.put(Param.startBytes);

		// Skip header
		final int loadPos = outputBuffer.position() + Param.headerLen;
		outputBuffer.position(loadPos);

		// put Encrypted data
		int encryptedDataLen = 0;
		try {
			encryptedDataLen = cipher.doFinal(inputBuffer, outputBuffer);
		} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			return false;
		}

		// Put crc32 value
		CRC32 crc32 = new CRC32();
		crc32.update(outputBuffer.array(), loadPos, encryptedDataLen);
		final int crcVal = (int) crc32.getValue();
		outputBuffer.putInt(crcVal);

		// Put end bytes
		outputBuffer.put(Param.endBytes);

		// Flip
		outputBuffer.flip();

		// Put header
		outputBuffer.position(Param.startBytes.length);
		final int loadLen = encryptedDataLen + Param.CRCLen + Param.endBytes.length;
		intToBytes(loadLen, headerBytes);
		outputBuffer.put(headerBytes);

		outputBuffer.position(0);
		return true;
	}

	private void copyInputInBuffer(ByteBuffer input)
	{
		inputBuffer.clear();
		inputBuffer.position(Param.randomLenInLoad);
		inputBuffer.put(input);
		inputBuffer.flip();
	}

	private void writeRandomInInputBuf()
	{
		inputBuffer.mark();
		inputBuffer.position(0);

		random.nextBytes(randomBytes);
		inputBuffer.put(randomBytes);

		try {
			inputBuffer.reset();
		} catch (InvalidMarkException e) {
		}
	}

	private static Key getPublicKey()
	{
		byte[] publicKey = Base64.decode(Param.encodePubicKey, Base64.DEFAULT);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);

		try {
			KeyFactory keyFactory = KeyFactory.getInstance(Param.keyFactoryAlgorithm);
			return keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static void intToBytes(int val, byte[] out)
	{
		for (int i = out.length - 1; i >= 0; --i) {
			out[i] = (byte) (val & 0xFF);
			val >>= 8;
		}
	}

	private SubBytesFinder startBytesFinder = new SubBytesFinder(Param.startBytes);
	private Random random = new Random(System.currentTimeMillis());
	private byte[] randomBytes = new byte[Param.randomLenInLoad];
	private byte[] headerBytes = new byte[Param.headerLen];
	private Cipher cipher;
	private ByteBuffer outputBuffer;
	private ByteBuffer inputBuffer;
}
