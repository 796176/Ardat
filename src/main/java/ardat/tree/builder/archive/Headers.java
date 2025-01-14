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
import io.Channels;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Headers is a class containing static methods related to header processing.
 */
public class Headers {

	/**
	 * Returns the path of the entity.
	 * @param header the header
	 * @return the path of the entity
	 */
	public static String getRelativePath(String header) {
		assert header != null;

		Matcher matcher = Pattern.compile("filepath [^\n]+").matcher(header);
		if (!matcher.find()) {
			throw new ArchiveCorruptedException("File header corrupted: the filepath property is absent: " + header);
		}
		String filename = matcher.group();
		return filename.substring(filename.indexOf(' ') + 1);
	}

	/**
	 * Returns the size of the file descriptor associated with this header.
	 * @param header the header
	 * @return the size of the file descriptor
	 */
	public static long getFileSize(String header) {
		assert header != null;

		Matcher matcher = Pattern.compile("size [\\da-f]{16}").matcher(header);
		if (!matcher.find()) {
			throw new ArchiveCorruptedException("File header corrupted: the size property is absent: " + header);
		}
		String sizeString = matcher.group();
		return Long.parseLong(sizeString.substring(sizeString.indexOf(' ') + 1), 16);
	}

	/**
	 * Returns the header starting from the current channel position and stops when reaches an empty line.
	 * @param sbc the channel to retrieve the header from
	 * @return the header
	 * @throws IOException if some I/O errors occur
	 */
	public static String retrieve(SeekableByteChannel sbc) throws IOException {
		assert sbc != null;

		try {
			long pos = sbc.position();
			int maxHeaderSize = 5 * 1024;
			int maxHeaderLineSize = 1024;
			StringBuilder header = new StringBuilder(maxHeaderSize);
			String headerLine;
			while (!(headerLine = Channels.readLine(sbc, maxHeaderLineSize)).isBlank()) {
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
}
