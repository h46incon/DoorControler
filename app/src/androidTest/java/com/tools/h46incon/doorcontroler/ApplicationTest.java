package com.tools.h46incon.doorcontroler;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.tools.h46incon.doorcontroler.Message.MessageDecoder;

import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

	public ApplicationTest()
	{
		super(Application.class);
	}

	public void testMessMessageDecode() throws Exception
	{
		String msgStr[] = new String[]{
				// message without start bytes
				"FDB18541","0017",
				"FDB18542","0017",

				// incomplete message
				"FDB18540","0017",
				"A5A500005A5A0000A5A500005A5A00",

		};

		byte[][] msgs = new byte[msgStr.length][];
		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = strToBytes(msgStr[i]);
		}

		MessageDecoder decoder = new MessageDecoder();

		for (byte[] msg : msgs) {
			List<byte[]> result = decoder.decode(msg, msg.length);
			assertEquals(result.size(), 0);
		}
	}

	public void testMessageDecode() throws Exception
	{
		String msgStr[] = new String[]{
				// A valid message
				// This is a encrypted message "Hello RSA"
				"FDB18540","0088",
					"83272D96CFC657AF938BE6A1A0D751CA",
					"FE9CCA7D1C7E55853A0044F06233E6F8",
					"7F1A47B6B9E793C17A8D7F46B5559C06",
					"193B81C31358662407845230D0ADC834",
					"2523038078314699C0940EF388090791",
					"6B6D815547723379C91207FFB273470C",
					"0F9EBFF888D80A2FB521142E0545102A",
					"B7F05F8E818DF9EF7528555F5B55D770",
				"CAD4FE6D","4085B1FD"
		};

		byte[][] msgs = new byte[msgStr.length][];
		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = strToBytes(msgStr[i]);
		}

		MessageDecoder decoder = new MessageDecoder();

		for (byte[] msg : msgs) {
			List<byte[]> decode = decoder.decode(msg, msg.length);
			if (!decode.isEmpty()) {
				byte[] msgDecode = decode.get(0);
				String strDecode = new String(msgDecode);
				assertEquals(strDecode, "Hello RSA");
			}
		}
	}

	private byte[] strToBytes(String hexStr)
	{
		int size = (hexStr.length() + 1) / 2;
		byte[] result = new byte[size];

		int str_i = 0;
		if (hexStr.length() % 2 == 1) {
			result[0] = (byte) Integer.parseInt(hexStr.substring(0, 1), 16);
			str_i += 1;
		} else {
			result[0] = (byte) Integer.parseInt(hexStr.substring(0, 2), 16);
			str_i += 2;
		}

		for (int byte_i = 1; byte_i < result.length; ++byte_i) {
			result[byte_i] =
					(byte) Integer.parseInt(hexStr.substring(str_i, str_i+2), 16);
			str_i += 2;
		}

		return result;
	}

}