package com.tools.h46incon.doorcontroler.StreamSplitter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by h46incon on 2015/2/17.
 * Find pattern using KMP
 * It could find pattern which may dispersed on many string
 */
class SubBytesFinder{
	public SubBytesFinder(byte[] pattern){
		this.pattern = pattern.clone();
		this.nextTable = BuildNextTable(pattern);
	}

	public void reset()
	{
		this.patIndex = 0;
	}

	// skip all data until read a start bytes
	// return true if found, false if not
	public boolean skipTillPattern(ByteBuffer buf)
	{
		if (!buf.hasRemaining()) {
			return false;
		}

		byte b = buf.get();
		while (true) {
			if (patIndex == -1 || b == pattern[patIndex]) {
				// match
				++patIndex;
				// test all match
				if (patIndex == pattern.length) {
					return true;
				} else {
					// try read next byte
					if (buf.hasRemaining()) {
						b = buf.get();
					} else {
						return false;
					}
				}
			} else {
				// skip
				patIndex = nextTable[patIndex];
			}

		}
		// Unreachable statement...
		// return false;
	}

	// Slip buf to buffers by pattern
	// The first buffer is empty if the pattern exist in the front of buf.
	public List<ByteBuffer> slip(ByteBuffer buf)
	{
		ArrayList<ByteBuffer> result = new ArrayList<>();
		ByteBuffer last;
		while (true) {
			last = buf.duplicate();
			if (skipTillPattern(buf)) {
				// Find a available piece
				if (buf.position() - last.position() >= pattern.length) {
					last.limit(buf.position() - pattern.length);
					result.add(last);
				}

				// Add a "null" to indicate a pattern is found
				result.add(null);
			} else {
				last.limit(buf.position());
				result.add(last);
				break;
			}
		}

		return result;
	}

	private static int[] BuildNextTable(byte[] pattern)
	{
		int[] nextTable = new int[pattern.length];
		int j = 0;
		int t = nextTable[0] = -1;

		while (j < pattern.length-1) {
			if (0 > t || pattern[j] == pattern[t]) {
				j++;
				t++;
				nextTable[j] =
						pattern[j] != pattern[t] ? t : nextTable[t];
			} else {
				//失配
				t = nextTable[t];
			}
		}

		return nextTable;
	}

	private final byte[] pattern;
	private final int[] nextTable;
	private int patIndex = 0;
}


