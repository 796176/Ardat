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

package ardat.tree.builder.archive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Headers {
	public static String getRelativePath(String header) {
		Matcher matcher = Pattern.compile("filepath [^\n]+").matcher(header);
		matcher.find();
		String filename = matcher.group();
		return filename.substring(filename.indexOf(' ') + 1);
	}

	public static long getFileSize(String header) {
		Matcher matcher = Pattern.compile("size [\\da-f]{16}").matcher(header);
		matcher.find();
		String sizeString = matcher.group();
		return Long.parseLong(sizeString.substring(sizeString.indexOf(' ') + 1), 16);
	}

	public static String retrieve(SeekableByteChannel sbc) throws IOException {
		long pos = sbc.position();
		int headerSize = Headers.getSize(sbc);
		ByteBuffer buffer = ByteBuffer.allocate(headerSize);
		sbc.read(buffer);
		buffer.flip();
		sbc.position(pos);
		return new String(buffer.array());
	}

	public static int getSize(SeekableByteChannel sbc) throws IOException {
		long pos = sbc.position();
		int headerSize = 0;
		String headerLine;
		while(!(headerLine = Headers.readLine(sbc)).isBlank()) {
			headerSize += headerLine.length() + 1;
		}
		sbc.position(pos);
		return ++headerSize;
	}


	public static String readLine(SeekableByteChannel sbc) throws IOException {
		StringBuilder line = new StringBuilder();
		while (sbc.position() < sbc.size()) {
			ByteBuffer buffer = ByteBuffer.allocate(1);
			int read = sbc.read(buffer);
			buffer.flip();
			if (read <= 0 || buffer.get(0) == '\n') return line.toString();
			line.append(Character.toChars(buffer.get(0)));
		}

		return null;
	}
}
