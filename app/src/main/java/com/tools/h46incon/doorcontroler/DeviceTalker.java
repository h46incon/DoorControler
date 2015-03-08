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
		this.deviceMAC = deviceMAC;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.encrypter.setKey(deviceMAC);
	}

	public boolean shakeHand() throws IOException
	{
		clearInput();
		int byteRead = -1;
		outputStream.write(cRequireSimpleResponse);
		outputStream.flush();
		byteRead = inputStream.read();

		if (byteRead != cDeviceSimpleResponse) {
			return false;
		} else {
			// Enter stream communicate to do complex communication
			outputStream.write(cEnterStreamCommunicate);
			return true;
		}
	}

	public boolean verifyDevice() throws IOException
	{
		dataBuf.clear();
		dataBuf.put(cRequireVerify);

		sendDataBuf();

		readPackages();
		if (packList.isEmpty()) {
			return false;
		}

		for (byte[] pack : packList) {
			if (pack[0] == cCommandResonse) {
				final byte[] expectMacAddr = encrypter.getMacAddr();
				// Check size
				if (pack.length == expectMacAddr.length + 1) {
					// Check equal
					int i;
					for (i = 0; i < pack.length; ++i) {
						if (expectMacAddr[i] != pack[i + 1]) {
							break;
						}
					}
					if (i == pack.length) {
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
		for (char k : key) {
			dataBuf.putChar(k);
		}

		readPackages();
		if (packList.isEmpty()) {
			return false;
		}

		for (byte[] pack : packList) {
			if (pack[0] == cCommandResonse) {
				return true;
			}
		}
		return false;
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
				if (!packs.isEmpty()) {
					packList.addAll(packs);
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

	private static final byte cCommandResonse = (byte) 0xFF;
	private static final byte cCommandFailed = (byte) 0xFD;
	private static final byte cRequireVerify = (byte) 0xbc;
	private static final byte cOpenDoor = (byte) 0x69;

	private static final String TAG = "DevTalker";

	private InputStream inputStream;
	private OutputStream outputStream;
	private String deviceMAC;

	private Encrypter encrypter = new Encrypter();
	private MessageEncoder messageEncoder = new MessageEncoder();
	private MessageDecoder messageDecoder = new MessageDecoder();
	private List<byte[]> packList = new LinkedList<>();

	private static final int kBufferSize = 1024;
	private ByteBuffer dataBuf = ByteBuffer.allocate(kBufferSize);
	private byte[] inputBuf = new byte[kBufferSize];
}
