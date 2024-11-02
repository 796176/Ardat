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

package ardat.tree.builder;

import ardat.tree.ArchiveEntity;
import ardat.tree.ArchiveEntityFactory;
import io.SharedSeekableByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchiveTreeBuilder extends TreeBuilder {

	private final Path archPath;

	private final LinkedList<SharedSeekableByteChannel> archivedEntities = new LinkedList<>();

	private ArchiveTreeBuilder(Path archive) throws IOException {
		assert archive != null;

		archPath = archive;
		checkArchiveMetadata();
		cacheEntities();
	}

	public static ArchiveTreeBuilder getArchiveTreeBuilder(Path archive) throws IOException {
		assert archive != null;

		return new ArchiveTreeBuilder(archive);
	}

	@Override
	protected ArchiveEntity getRoot() throws IOException {
		return ArchiveEntityFactory.fromChannel(archivedEntities.getFirst());
	}

	@Override
	protected ArchiveEntity[] getChildren(ArchiveEntity entity) throws IOException {
		String relativeName = String.join("/", entity.getName());
		int index = 0;
		while (!relativeName.equals(getName(archivedEntities.get(index))) && ++index < archivedEntities.size() - 1);
		ArchiveEntity[] children = new ArchiveEntity[getChildrenCount(archivedEntities.get(index))];
		if (children.length == 0) return new ArchiveEntity[0];

		Iterator<SharedSeekableByteChannel> iterator = archivedEntities.iterator();
		int childrenIndex = 0;
		while (iterator.hasNext()) {
			SharedSeekableByteChannel sbc = iterator.next();
			String childName = getName(sbc);
			Matcher matcher = Pattern.compile("^" + relativeName + "/[^/]+$").matcher(childName);
			if (matcher.find()) children[childrenIndex++] = ArchiveEntityFactory.fromChannel(sbc);
		}

		return children;
	}

	private void checkArchiveMetadata() throws IOException {
		try (SeekableByteChannel sbc = Files.newByteChannel(archPath, StandardOpenOption.READ)) {
			sbc.position(0);
			String line = readLine(sbc);
			if (!line.equals("ardat")) throw new IOException("The archive file is corrupted");
		}
	}

	private void cacheEntities() throws IOException {
		SeekableByteChannel sbc = Files.newByteChannel(archPath, StandardOpenOption.READ);
		sbc.position(getArchiveMetadataSize());
		while (sbc.position() < sbc.size()) {
			long fileSize = getFileSize(sbc);
			int headerSize = getHeaderSize(sbc);
			archivedEntities.add(new SharedSeekableByteChannel(sbc, sbc.position(), headerSize + fileSize));
			sbc.position(sbc.position() + headerSize + fileSize);
		}
	}

	private String getName(SeekableByteChannel sbc) throws IOException {
		String header = retrieveHeader(sbc);
		Matcher matcher = Pattern.compile("filepath [^\n]+").matcher(header);
		matcher.find();
		String filename = matcher.group();
		return filename.substring(filename.indexOf(' ') + 1);
	}

	private int getChildrenCount(SeekableByteChannel sbc) throws IOException {
		String header = retrieveHeader(sbc);
		Matcher matcher = Pattern.compile("children \\d+").matcher(header);
		matcher.find();
		String childrenString = matcher.group();
		return Integer.parseInt(childrenString.substring(childrenString.indexOf(' ') + 1));
	}

	private long getFileSize(SeekableByteChannel sbc) throws IOException{
		String header = retrieveHeader(sbc);
		Matcher matcher = Pattern.compile("size [\\da-f]{16}").matcher(header);
		matcher.find();
		String sizeString = matcher.group();
		return Long.parseLong(sizeString.substring(sizeString.indexOf(' ') + 1), 16);
	}

	private String retrieveHeader(SeekableByteChannel sbc) throws IOException {
		long pos = sbc.position();
		int headerSize = getHeaderSize(sbc);
		ByteBuffer buffer = ByteBuffer.allocate(headerSize);
		sbc.read(buffer);
		buffer.flip();
		sbc.position(pos);
		return new String(buffer.array());
	}

	private int getHeaderSize(SeekableByteChannel sbc) throws IOException {
		long pos = sbc.position();
		int headerSize = 0;
		String headerLine;
		while(!(headerLine = readLine(sbc)).isBlank()) {
			headerSize += headerLine.length() + 1;
		}
		sbc.position(pos);
		return ++headerSize;
	}


	private String readLine(SeekableByteChannel sbc) throws IOException {
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

	private long getArchiveMetadataSize() throws IOException {
		try (SeekableByteChannel sbc = Files.newByteChannel(archPath, StandardOpenOption.READ)) {
			long headerSize = readLine(sbc).length() + 1;
			String linesAmount = readLine(sbc);
			headerSize += linesAmount.length() + 1;
			for (int i = 2; i < Integer.parseInt(linesAmount); i++) {
				headerSize += readLine(sbc).length() + 1;
			}
			return headerSize;
		}
	}
}
