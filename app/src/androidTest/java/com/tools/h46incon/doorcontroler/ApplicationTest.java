package com.tools.h46incon.doorcontroler;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.tools.h46incon.doorcontroler.Message.MessageDecoder;
import com.tools.h46incon.doorcontroler.Message.MessageEncoder;
import com.tools.h46incon.doorcontroler.Message.RSADecoder;
import com.tools.h46incon.doorcontroler.Message.RSAEncoder;

import java.nio.ByteBuffer;
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
					"006B9A453F6AC7897B32DB6F1266A3F0",
					"008F876311A3CE1BC66DAEB0DEEE8A23",
					"CD64E420F4F27B32EAB87ABAB36E29F6",
					"C29DF9067E7AFE2E0544B3AB9CB03BC8",
					"9F45529A958B912406694789110325D3",
					"75A5DEBAAA711C4A81641F01AEFDAAB1",
					"FBA5B0D308D3E9794F211ADC5274B0C0",
					"76CF40D27C3385643081D41D9DA87973",
				"6F2211C2","4085B1FD"
		};

		byte[][] msgs = new byte[msgStr.length][];
		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = strToBytes(msgStr[i]);
		}

		MessageDecoder decoder = new MessageDecoder();

		RSADecoder rsaDecoder = new RSADecoder();
		for (byte[] msg : msgs) {
			List<byte[]> decode = decoder.decode(msg, msg.length);
			if (!decode.isEmpty()) {
				byte[] msgDecode = decode.get(0);
				ByteBuffer decrypt = rsaDecoder.decode(ByteBuffer.wrap(msgDecode));
				byte[] d = new byte[decrypt.remaining()];
				decrypt.get(d);
				String strDecode = new String(d);
				assertEquals(strDecode, "Hello RSA");
			}
		}
	}

	public void testEncode() throws Exception
	{
		String dataStr = "A5A500005A5A0000";

		byte[] data = strToBytes(dataStr);
		ByteBuffer input = ByteBuffer.wrap(data);

		RSAEncoder rsaEncoder = new RSAEncoder();
		ByteBuffer encrypedData = rsaEncoder.encode(input);
		assertEquals(encrypedData.remaining(), 128);

		MessageEncoder messageEncoder = new MessageEncoder();
		ByteBuffer encode = messageEncoder.encode(encrypedData);
		assertEquals(encode.remaining(), 128 + 4 + 2 + 4 + 4);
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