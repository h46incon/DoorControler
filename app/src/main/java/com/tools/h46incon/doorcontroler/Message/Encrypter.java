package com.tools.h46incon.doorcontroler.Message;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by h46incon on 2015/2/27.
 * This class is used to encrypt data
 * it will encrypt data with device MAC address
 * First it will put 2 random bytes.
 *  The number of bit 1 in first bytes indicate the position of MAC address
 *      The target will act as KEY1
 *  And the second bytes will act as KEY2
 * The message's encode key is KEY = KEY1 ^ KEY2
 * And the message will by calc by ^KEY
 */
public class Encrypter {
	public void setKey(String MACAddr)
	{
		// Trans to bytes
		for (int i = 0; i < macAddrSize; ++i) {
			int strI = i * 3;
			macAddr[i] = (byte) Integer.parseInt(
					MACAddr.substring(strI, strI + 2), 16);
		}
	}

	public final byte[] getMacAddr()
	{
		return macAddr;
	}

	public ByteBuffer encrypt(ByteBuffer input)
	{
		final int needBufLen = input.remaining() + 2;
		if (outputBuf.capacity() < needBufLen) {
			outputBuf = ByteBuffer.allocate(needBufLen);
		}
		outputBuf.clear();

		encrypt(input, outputBuf);
		outputBuf.flip();
		return outputBuf.duplicate();
	}

	public void encrypt(ByteBuffer input, ByteBuffer output)
	{
		if (output.remaining() < input.remaining() + 2) {
			throw new IllegalArgumentException("Output buffer is not enough");
		}

		byte random1 = (byte) random.nextInt();
		byte random2 = (byte) random.nextInt();
		byte key = calcKey(random1, random2);

		output.put(random1);
		output.put(random2);
		doXOR(input, key, output);
	}

	public ByteBuffer decrypt(ByteBuffer input)
	{
		final int outBufLen = input.remaining() - 2;
		if (outBufLen < 0) {
			throw new IllegalArgumentException("Input message is incomplete");
		}

		if (outBufLen > outputBuf.capacity()) {
			outputBuf = ByteBuffer.allocate(outBufLen);
		}
		outputBuf.clear();

		decrypt(input, outputBuf);
		outputBuf.flip();
		return outputBuf.duplicate();
	}

	public void decrypt(ByteBuffer input, ByteBuffer output)
	{
		if (input.remaining() < 2) {
			throw new IllegalArgumentException("Input message is incomplete");
		}
		if (output.remaining() < input.remaining() - 2) {
			throw new IllegalArgumentException("Output buffer is not enough");
		}
		byte random1 = input.get();
		byte random2 = input.get();
		byte key = calcKey(random1, random2);

		doXOR(input, key, output);
	}

	private byte calcKey(byte random1, byte ramdom2)
	{
		int bitCount = bitCount(random1 & 0xFF);
		byte key1 = macAddr[bitCount % macAddr.length];
		return (byte)(key1 ^ ramdom2);
	}

	private void doXOR(ByteBuffer input, byte key, ByteBuffer output)
	{
		while (input.hasRemaining()) {
			byte d = input.get();
			output.put((byte)(d ^ key));
		}
	}

	private int bitCount(int i)
	{
		if (i < 0) {
			throw new IllegalArgumentException("请输入一个非负整数.");
		}

		int count = 0;
		while (i != 0) {
			i &= (i - 1);
			++count;
		}

		return count;
	}

	private Random random = new Random();
	private static final int defBufSize = 1024;
	private ByteBuffer outputBuf = ByteBuffer.allocate(defBufSize);
	private final int macAddrSize = 6;
	private byte[] macAddr = new byte[macAddrSize];
}
