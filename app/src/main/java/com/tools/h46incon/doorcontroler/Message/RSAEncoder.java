package com.tools.h46incon.doorcontroler.Message;

import android.util.Base64;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

/**
 * Created by Administrator on 2015/2/26.
 */
public class RSAEncoder {
	public RSAEncoder() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException
	{
		cipher = Cipher.getInstance(Param.encryptAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
		encryptBuf = ByteBuffer.allocate(1024);
		outputBuf = ByteBuffer.allocate(keyLen / 8 + 1);
	}

	public final ByteBuffer encode(ByteBuffer input)
	{
		final int totalLen = input.remaining() + Param.randomLenInLoad;

		if (totalLen > encryptBuf.capacity()) {
			encryptBuf = ByteBuffer.allocate(totalLen);
		}
		encryptBuf.clear();

		// Write random bytes
		random.nextBytes(randomBytes);
		encryptBuf.put(randomBytes);

		// put input
		encryptBuf.put(input);

		encryptBuf.flip();

		// Encrypte data
		outputBuf.clear();
		try {
			cipher.doFinal(encryptBuf, outputBuf);
		} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
		outputBuf.flip();

		return outputBuf.asReadOnlyBuffer();
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

	private Cipher cipher;
	private ByteBuffer encryptBuf;
	private ByteBuffer outputBuf;
	private final int keyLen = 1024;
	private Random random = new Random();
	private byte[] randomBytes = new byte[Param.randomLenInLoad];
}
