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

import ardat.exceptions.ArchiveCorruptedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Headers {
	public static String getRelativePath(String header) {
		Matcher matcher = Pattern.compile("filepath [^\n]+").matcher(header);
		if (!matcher.find()) {
			throw new ArchiveCorruptedException("File header corrupted: the filepath property is absent: " + header);
		}
		String filename = matcher.group();
		return filename.substring(filename.indexOf(' ') + 1);
	}

	public static long getFileSize(String header) {
		Matcher matcher = Pattern.compile("size [\\da-f]{16}").matcher(header);
		if (!matcher.find()) {
			throw new ArchiveCorruptedException("File header corrupted: the size property is absent: " + header);
		}
		String sizeString = matcher.group();
		return Long.parseLong(sizeString.substring(sizeString.indexOf(' ') + 1), 16);
	}

	public static String retrieve(SeekableByteChannel sbc) throws IOException {
		try {
			long pos = sbc.position();
			int maxHeaderSize = 5 * 1024;
			int maxHeaderLineSize = 1024;
			StringBuilder header = new StringBuilder(maxHeaderSize);
			String headerLine;
			while (!(headerLine = Headers.readLine(sbc, maxHeaderLineSize)).isBlank()) {
				header.append(headerLine).append('\n');
				if (header.length() > maxHeaderSize) {
					throw new ArchiveCorruptedException("The header exceeded the maximum length of " + maxHeaderSize);
				}
			}
			sbc.position(pos);
			return header.append('\n').toString();
		} catch (NullPointerException exception) {
			throw new ArchiveCorruptedException(
				"End of file had been reached before attempting to read the header"
			);
		}
	}

	public static String readLine(SeekableByteChannel sbc, int maxSize) throws IOException {
		StringBuilder line = new StringBuilder();
		while (sbc.position() < sbc.size()) {
			ByteBuffer buffer = ByteBuffer.allocate(1);
			int read = sbc.read(buffer);
			buffer.flip();
			if (read <= 0 || buffer.get(0) == '\n' || maxSize == line.length()) return line.toString();
			line.append(Character.toChars(buffer.get(0)));
		}

		return null;
	}
}
