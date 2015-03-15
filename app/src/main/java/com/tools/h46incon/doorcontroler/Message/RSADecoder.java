package com.tools.h46incon.doorcontroler.Message;

import android.util.Base64;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

/**
 * Created by h46incon on 2015/2/26.
 *
 */
public class RSADecoder {
	public RSADecoder() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException
	{
		cipher = Cipher.getInstance(Param.encryptAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, getRSAPrivateKey());

	}

	public ByteBuffer decode(ByteBuffer input)
	{

		try {
			buffer.clear();
			cipher.doFinal(input, buffer);

			buffer.flip();
			// Skip random byte
			if (buffer.remaining() < Param.randomLenInLoad) {
				return null;
			}
			buffer.position(buffer.position() + Param.randomLenInLoad);

			return buffer;
			// Note: ACK
		} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
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

	private ByteBuffer buffer = ByteBuffer.allocate(4096);
	private Cipher cipher;
}
