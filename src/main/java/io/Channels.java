/*
 * Ardat is a file archiver
 * Copyright (C) 2024 Yegore Vlussove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;

/**
 * Channels is a class containing static methods related to channels.
 */
public class Channels {

	/**
	 * The invocation is analogous to Channels.readLine(ReadableByteChannel, Integer.MAX_VALUE).
	 */
	public static String readLine(ReadableByteChannel channel) throws IOException {
		return readLine(channel, Integer.MAX_VALUE);
	}

	/**
	 * Attempts to read a line from the given channel. The position of the channel is moved forward by the length of the
	 * return line plus a line feeder. If the length of the line is equal to maxSize but didn't reach a line feeder,
	 * the method moves the position of the channel by the length of this line.
	 * @param channel the channel to read a line from
	 * @param maxSize the maximum length of the line
	 * @return the line or null if it's the end of the channel
	 * @throws IOException If some I/O error occurs
	 */
	public static String readLine(ReadableByteChannel channel, int maxSize) throws IOException {
		assert channel != null && maxSize > 0;

		StringBuilder lineBuilder = new StringBuilder();
		String line = null;
		ByteBuffer buffer = ByteBuffer.allocate(1);
		while (channel.read(buffer) > 0) {
			buffer.flip();
			if (buffer.get(0) == '\n' || maxSize == lineBuilder.length()) break;
			lineBuilder.append(Character.toChars(buffer.get(0)));
			line = lineBuilder.toString();
			buffer.clear();
		}

		return line;
	}

	/**
	 * The invocation is analogues to Channel.readLine(SeekableByteChannel, Integer.MAX_VALUE).
	 */
	public static String readLine(SeekableByteChannel channel) throws IOException {
		return 	readLine(channel, Integer.MAX_VALUE);
	}

	/**
	 * The invocation is analogous to {@link Channels#readLine(ReadableByteChannel, int)}, with the exception that it
	 * reads the data in a bulk potentially improving the performance.
	 * @param channel the channel to read a line from
	 * @param maxSize the maximum length of the line
	 * @return the line or null if it's the end of the channel
	 * @throws IOException If some I/O error occurs
	 */
	public static String readLine(SeekableByteChannel channel, int maxSize) throws IOException {
		assert channel != null && maxSize > 0;

		StringBuilder lineBuilder = new StringBuilder();
		String line = null;
		ByteBuffer byteBuffer = ByteBuffer.allocate(512);
		while (channel.read(byteBuffer) > 0) {
			byteBuffer.flip();
			CharBuffer charBuffer = Charset.defaultCharset().decode(byteBuffer);

			int lfIndex = charBuffer.toString().indexOf('\n');
			if (lfIndex > -1) {
				lineBuilder.append(charBuffer, 0, lfIndex);
				channel.position(channel.position() - byteBuffer.limit() + lfIndex + 1);
				line = lineBuilder.toString();
				break;
			}

			lineBuilder.append(charBuffer);
			line = lineBuilder.toString();
			byteBuffer.clear();
			byteBuffer.limit(Math.min(byteBuffer.capacity(), maxSize - lineBuilder.length()));
		}

		return line;
	}
}
