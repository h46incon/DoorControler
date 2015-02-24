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
	// Message field Info:
	private final static byte[] startBytes = new byte[]{(byte) 0xFD, (byte) 0xB1, (byte) 0x85, (byte) 0x40};
	private final static byte[] endBytes = new byte[]{(byte) 0x40, (byte) 0x85, (byte) 0xB1, (byte) 0xFD};
	private final static int headerLen = 2;
	private final static int CRCLen = 4;
	private final static int packageMinLen = endBytes.length + headerLen + CRCLen;
	private final static String privateKey =
			"" +
					"MIICdXAgIIBBAAADAKBNBgQgkDNqhUFkiSPG9oRw0JjBAJPQEZWFAwDASS5C" +
					"AijmADcwgyLgJ81cAEsgEeyAAVNoGb/BAKmM1waQVQgI+TNhEQZmMzik9gZl" +
					"bvbAN7ZLmhGKMI1Nz6xIvaIzUfcSxvM7JaWU1eqv8hNqbH+Bp44CBSlM1ddB" +
					"nsZOKmjBmHB9vDytmWUEYx+jXx7rFe5ohq79y/K8xNopZ2r6qyjE0QIf7T0j" +
					"hejKVCc12qqxmtdaM0ScEv0PJWvZTfHH7ySHtEx7mULrvLk8ofC2jfZavq9K" +
					"NYsAh+tPRqI6MOsJyT9qqUW13QURKbE/Rrxa9L38ffQJILBTFXlQstHuRYH8" +
					"JaR9mCQr1IDizAQ62ABogAo6xGAP1GaRZxYBR1psSj6vElqvdEx9AmIsFlye" +
					"WTl0d0kgdXQpEdRJAOYgM6+BAn+AEMRCg+1YAhPZrqjFjzlWmItPqF4Woj6T" +
					"G5LYiUgXJX0OXy5SRhydBp91EZ95jvtr6WKf4AYxHsw7WPKE+dnqPbdOUozi" +
					"0JNXi0QPraJktX4SBrlfThfLmScHKl1n1iCn1jHesrAdB8+XU77sEIbm2Yx7" +
					"5B8LFceaijI1E915cz41mZeTXBSZDQApd1MoqXr3rHQ2rV13sLmKk+jVz+4U" +
					"LW3Os1qdNegqzEdBlNLc6YsISRlXjP6aIBb3NG82DjletxLACA7lyyQVEmbH" +
					"SOEnH3/ioXGdxCEwGVYa9ytEXqklwrbVFo7+D3sC2nGvIQIq3DAEBQAQVBBC" +
					"dJtkbu+UmSxpGWoYvtRQJOXBANZPqk1WV2Qq96XpOaKmSt6LC9qwM97f5Cpo" +
					"BFclS1CX2zrU4HT8LasoDwZX+XO3kiEQOl4wX2iIgNzekdgbZ3rxAQAn+pc8" +
					"flRLXBAr2oGaaAAUcaxS+74HObaf6bVe+UqWLb4pLZoylEv1oenlg9y+8xCn" +
					"Auk1xEb6aPcIGNb089ICQqaQDI4RvhI6UMBdeX+PH1w45UykYoariPY1YYz2" +
					"PC0kNVHO91Lz7y0Gbpxbd4qrpHc2CcBsQmvfaRF5+5cQoFRm1fgBjwtEury3" +
					"SKtsvxA4qUFjmQnnvZGcB1JFlqRZ0mL1M0Cz5QQ7YD6PqllS5avyGaTlLpk1" +
					"eiwnisD3ZH+RgaAnAZUkAlRHcr+NN4+eHYJgfOQH2gvIENbZNKTKdv4JheOL" +
					"h4xr7b1DHcMfJcKbjm2Q5kTAiXBATEHYppmR2utGaOiHPuKk/NJOrmyzA+xc" +
					"zUgsYkkFtk9j6IuuJMLJvyzKPv2boF91/lO6qPCQ16Apd1/det53EEDDdsF/" +
					"VyIXsHpo7G2tAcQkBJ/pSvHr5y1Vf69DhmmkDlHRzEvekhzRTn+XKnvAxlib" +
					"46SKw8pHqdal4JYwUPvZaJwrANcnYembxiB5OtP8CAkC7EAi20b4N+lnkHXM" +
					"PjxE++OzEZGAMK4sKtWz3Nj+z5DdmTvozc+/cxmZt23aNa6jKdg8/rEh0H2J" +
					"Wuf1tkKzAJtkEQYAmxLY5t0V3rLjz+KKbo5zep70d3A/7RZ4MWdboNTqaM+e" +
					"Ue2PXD6b6kud7chuYpSxV9XUgp4Act2tGUYnJJwcMJAVrB39GDTDaXhvO4Hy" +
					"+x9+PiBBwGTvYSnjMSYzKS4Uqa+3CwxFw3yMWW4pM0OS8QI21gE1N2KFaUdp" +
					"ihmHWhzA==";


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
		packageFormat.startBytes = startBytes;
		packageFormat.headerLen = headerLen;
		packageFormat.lenGetter = msgLenGetter;

		streamSplitter = new StreamSplitter(packageFormat);

		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, getRSAPrivateKey());

	}

	private Key getRSAPrivateKey()
	{
		// Decrypt private key
		StringBuilder stringBuilder = new StringBuilder(1024);
		int size = (privateKey.length() - 4 - 2) / 2;

		stringBuilder.append("MIIC");
		for (int i = 0; i < size; i++) {
			char c = privateKey.charAt(i * 2 + i % 2 + 4);
			stringBuilder.append(c);
		}
		stringBuilder.append("=");
		stringBuilder.append("=");
		String privateKey = stringBuilder.toString();

		// Gen private key
		byte[] privKeyByte = Base64.decode(privateKey, Base64.DEFAULT);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyByte);

		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<byte[]> decode(byte[] msg, int length)
	{
		List<byte[]> packages = streamSplitter.join(msg, length);
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
		if (pack.length < packageMinLen) {
			return null;
		}

		// Check end bytes
		for (int i = endBytes.length; i > 0; --i) {
			if (pack[pack.length - i] != endBytes[endBytes.length - i]) {
				return null;
			}
		}


		// Check CRC
		CRC32 crc32 = new CRC32();
		final int loadLen = pack.length - headerLen - CRCLen - endBytes.length;
		crc32.update(pack, headerLen, loadLen);
		// Need convert crc32 into int,
		// because I will convert 4bytes data into int later.
		int crcTarget = (int)crc32.getValue();

		// Get CRC value in package
		ByteBuffer byteBuffer = ByteBuffer.wrap(pack, headerLen + loadLen, CRCLen);
		byteBuffer.order(ByteOrder.BIG_ENDIAN);
		int crcExpect = byteBuffer.getInt();

		if (crcTarget != crcExpect) {
			return null;
		}

		return ByteBuffer.wrap(pack, headerLen, loadLen);
	}

	private StreamSplitter streamSplitter;
	private ByteBuffer buffer = ByteBuffer.allocate(4096);
	private Cipher cipher;
}
