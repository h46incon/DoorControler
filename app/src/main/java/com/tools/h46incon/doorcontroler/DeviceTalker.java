package com.tools.h46incon.doorcontroler;

import android.util.Log;

import com.tools.h46incon.doorcontroler.Message.Encrypter;
import com.tools.h46incon.doorcontroler.Message.MessageDecoder;
import com.tools.h46incon.doorcontroler.Message.MessageEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by h46incon on 2015/3/1.
 * This class is used for handle device communication protocol
 */
public class DeviceTalker {
	public DeviceTalker ()
	{

	}

	public void setStream(String deviceMAC, InputStream inputStream, OutputStream outputStream)
	{
		this.inputStream = null;
		try {
			this.clearInput();
		} catch (IOException e) {
			// do nothing
		}

		this.deviceMAC = deviceMAC;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.encrypter.setKey(deviceMAC);
	}

	public boolean shakeHand() throws IOException
	{
		clearInput();
		byte byteRead;
		outputStream.write(cRequireSimpleResponse);
		outputStream.flush();
		byteRead = (byte)inputStream.read();

		if (byteRead != cDeviceSimpleResponse) {
			return false;
		}

		return EnterStreamCommunicateMode(3);
	}

	public boolean verifyDevice() throws IOException
	{
		dataBuf.clear();
		dataBuf.put(cRequireVerify);
		dataBuf.flip();

		sendDataBuf();
		readPackages();

		if (packList.isEmpty()) {
			throw new IOException("Device not responding");
		}

		for (byte[] pack : packList) {
			if (pack[0] == cCmdSuccess) {
				final byte[] expectMacAddr = encrypter.getMacAddr();
				// Check size
				if (pack.length == expectMacAddr.length + 1) {
					// Check equal
					int i;
					for (i = 0; i < expectMacAddr.length; ++i) {
						if (expectMacAddr[i] != pack[i + 1]) {
							break;
						}
					}
					if (i == expectMacAddr.length) {
						return true;
					}
					// else continue
				}
			}
		}
		return false;
	}

	public boolean openDoor(char[] key) throws IOException
	{
		dataBuf.clear();
		dataBuf.put(cOpenDoor);
		putKeyInBuffer(key, dataBuf);
		dataBuf.flip();

		sendDataBuf();
		readPackages();

		if (packList.isEmpty()) {
			throw new IOException("Device not responding");
		}

		for (byte[] pack : packList) {
			switch (pack[0]) {
				case cCmdSuccess:
					return true;
				case cKeyError:
					return false;
			}
		}

		throw new IOException("Error device responded");
	}

	public boolean changeOpenDoorKey(char[] adminKey, char[] oldKey, char[] newKey)
	{
		// TODO:
		return false;
	}

	public boolean changeAdminKey(char[] oldAdminKey, char[] newAdminKey)
	{
		// TODO:
		return false;
	}

	private static void putKeyInBuffer(char[] key, ByteBuffer buffer)
	{
		byte key_len = (byte)key.length;
		buffer.put(key_len);
		for (char k : key) {
			buffer.put((byte) k);
		}
	}

	private boolean EnterStreamCommunicateMode(int tryTimes) throws IOException
	{
		if (tryTimes <= 0)
		{
			return false;
		}

		// Enter stream communicate to do complex communication
		outputStream.write(cEnterStreamCommunicate);

		// Read random key
		int key2 = inputStream.read();
		// this key will send twice for check
		if (key2 == inputStream.read()) {
			encrypter.setKey2((byte)key2);
			return true;
		} else {
			// Try again
			return EnterStreamCommunicateMode(tryTimes - 1);
		}
	}

	private void sendDataBuf() throws IOException
	{
		clearInput();
		// Pack data
		ByteBuffer encrypted = encrypter.encrypt(dataBuf);
		ByteBuffer msgToSend = messageEncoder.encode(encrypted);

		int offset = msgToSend.position();
		int count = msgToSend.remaining();
		byte[] msgBytes = msgToSend.array();
		outputStream.write(msgBytes, offset, count);
	}

	private void readPackages() throws IOException
	{
		boolean hasGetPack = false;
		while (!hasGetPack) {
			// Use read() to block
			try {
				int firstData = inputStream.read();
				inputBuf[0] = (byte) firstData;
			} catch (ClosedByInterruptException e) {
				Log.d(TAG, "Not data available");
				return;
			}

			// Wait for more data
			try {
				final int waitMS = 10;
				Thread.sleep(waitMS);
			} catch (InterruptedException e) {

			}

			int dataLen = inputStream.available();
			if (dataLen > 0) {
				// Read data
				if (dataLen > inputBuf.length - 1) {
					dataLen = inputBuf.length -1;
				}
				inputStream.read(inputBuf, 1, dataLen);

				// Decode
				List<byte[]> packs = messageDecoder.decode(inputBuf, dataLen + 1);
				for (byte[] pack : packs) {
					// NOTE: performance
					ByteBuffer decrypt = encrypter.decrypt(ByteBuffer.wrap(pack));
					byte[] data = new byte[decrypt.remaining()];
					decrypt.get(data);
					packList.offer(data);
					hasGetPack = true;
				}
			}
		}

	}

	private void clearInput() throws IOException
	{
		packList.clear();
		messageDecoder.reset();
		if (inputStream != null) {
			int available = inputStream.available();
			inputStream.skip(available);
		}
	}

	private static final byte cRequireSimpleResponse = 0x38;
	private static final byte cDeviceSimpleResponse = (byte) 0x83;
	private static final byte cEnterStreamCommunicate = 0x76;

	private static final byte cCmdSuccess = (byte) 0x96;
	private static final byte cNotACmd = (byte) 0xFD;
	private static final byte cKeyError = (byte) 0x99;

	private static final byte cRequireVerify = (byte) 0xBC;
	private static final byte cOpenDoor = (byte) 0x69;
	private static final byte cChangeKey = (byte) 0x70;
	private static final byte cChangeAdminKey = (byte) 0x71;

	private static final String TAG = "DevTalker";

	private InputStream inputStream;
	private OutputStream outputStream;
	private String deviceMAC;

	private Encrypter encrypter = new Encrypter();
	private MessageEncoder messageEncoder = new MessageEncoder();
	private MessageDecoder messageDecoder = new MessageDecoder();
	private Queue<byte[]> packList = new LinkedList<>();

	private static final int kBufferSize = 1024;
	private ByteBuffer dataBuf = ByteBuffer.allocate(kBufferSize);
	private byte[] inputBuf = new byte[kBufferSize];
}
