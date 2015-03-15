package com.tools.h46incon.doorcontroler;

import android.util.Log;

import com.tools.h46incon.doorcontroler.Message.Encrypter;
import com.tools.h46incon.doorcontroler.Message.MessageDecoder;
import com.tools.h46incon.doorcontroler.Message.MessageEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by h46incon on 2015/3/1.
 * This class is used for handle device communication protocol
 * All I/O operations will not timeout.
 * It assume user has run these operations in a background thread (such as BGWorker),
 * and when user try to cancel this thread, it will throw a IOException("Device not responding")
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
		Log.d(TAG, "Start shake hand");
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
		Log.d(TAG, "Start verify device");
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
		Log.d(TAG, "Start open door");
		dataBuf.clear();
		dataBuf.put(cOpenDoor);
		putKeyInBuffer(key, dataBuf);
		dataBuf.flip();

		return sendKeyCommand();
	}

	private boolean sendKeyCommand() throws IOException
	{
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

		return false;
	}

	public boolean changeOpenDoorKey(char[] adminKey, char[] oldKey, char[] newKey) throws IOException
	{
		Log.d(TAG, "Start change open door key");
		dataBuf.clear();
		dataBuf.put(cChangeKey);
		putKeyInBuffer(adminKey, dataBuf);
		putKeyInBuffer(oldKey, dataBuf);
		putKeyInBuffer(newKey, dataBuf);
		dataBuf.flip();

		return sendKeyCommand();
	}

	public boolean changeAdminKey(char[] oldAdminKey, char[] newAdminKey) throws IOException
	{
		Log.d(TAG, "Start change admin key");
		dataBuf.clear();
		dataBuf.put(cChangeAdminKey);
		putKeyInBuffer(oldAdminKey, dataBuf);
		putKeyInBuffer(newAdminKey, dataBuf);
		dataBuf.flip();

		return sendKeyCommand();
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
		Log.d(TAG, "waiting for respond");
		final int waitTime = 100;       // 100 ms
		boolean hasGetPack = false;
		while (!hasGetPack) {
			// F**K, this blocking IO will not return even if try to cancel this thread.
			// Use read() to block
			//int firstData = inputStream.read();
			while (true) {
				if (inputStream.available() == 0) {
					try {
						// Log.v(TAG, "No data, sleeping...");
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
						Log.d(TAG, "Stop trying to read packages");
						return;
					}
				} else {
					break;
				}
			}
			Log.v(TAG, "new input data available, thread ID: " + Thread.currentThread().toString() );

			// Wait for more data
			try {
				final int waitMS = waitTime;
				Thread.sleep(waitMS);
			} catch (InterruptedException e) {

			}

			int dataLen = inputStream.available();
			if (dataLen > 0) {
				// Read data
				if (dataLen > inputBuf.length) {
					dataLen = inputBuf.length;
				}
				inputStream.read(inputBuf, 0, dataLen);

				List<byte[]> packs = messageDecoder.decode(inputBuf, dataLen);
				for (byte[] pack : packs) {
					// NOTE: performance
					ByteBuffer decrypt = encrypter.decrypt(ByteBuffer.wrap(pack));
					byte[] data = new byte[decrypt.remaining()];
					decrypt.get(data);
					packList.offer(data);
					hasGetPack = true;
					Log.d(TAG, "get package");
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
