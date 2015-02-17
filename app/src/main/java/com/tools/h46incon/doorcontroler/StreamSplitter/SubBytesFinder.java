package com.tools.h46incon.doorcontroler.StreamSplitter;

import java.nio.ByteBuffer;

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


