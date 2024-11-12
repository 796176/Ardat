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

import ardat.exceptions.ArchiveCorruptedException;
import ardat.tree.ArchiveEntity;
import ardat.tree.ArchiveEntityFactory;
import ardat.tree.builder.archive.Headers;
import ardat.format.Metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class ArchiveTreeBuilder extends TreeBuilder {

	public record ArchEntityInfo(long offset, String header) {}

	private final Path archPath;

	private final FileHierarchy hierarchy = new FileHierarchy(1024);

	private Path root;

	private final HashMap<Path, ArchEntityInfo> cachedInfo = new HashMap<>();

	private ArchiveTreeBuilder(Path archive) throws IOException {
		assert archive != null;

		archPath = archive;
		extractMetadataHeader();
		cacheEntities();
	}

	public static ArchiveTreeBuilder getArchiveTreeBuilder(Path archive) throws IOException {
		assert archive != null;

		return new ArchiveTreeBuilder(archive);
	}

	@Override
	protected ArchiveEntity getRoot() throws IOException {
		ArchEntityInfo info = cachedInfo.get(root);
		return ArchiveEntityFactory.fromArchive(info, archPath);
	}

	@Override
	protected ArchiveEntity[] getChildren(ArchiveEntity entity) throws IOException {
		String relativeName = String.join("/", entity.getName());
		Path[] childrenPaths = hierarchy.getChildren(Path.of(relativeName));
		ArchiveEntity[] children = new ArchiveEntity[childrenPaths.length];
		int childrenIndex = 0;
		for (Path p: childrenPaths) {
			ArchEntityInfo childInfo = cachedInfo.get(p);
			children[childrenIndex++] = ArchiveEntityFactory.fromArchive(childInfo, archPath);
		}

		return children;
	}

	private void extractMetadataHeader() throws IOException {
		try (SeekableByteChannel sbc = Files.newByteChannel(archPath, StandardOpenOption.READ)) {
			Metadata.MetadataBuilder builder = Metadata.getBuilder();
			while (builder.feedPropertyLine(readLine(sbc)));
			builder.build();
		}
	}

	private void cacheEntities() throws IOException {
		try(SeekableByteChannel sbc = Files.newByteChannel(archPath, StandardOpenOption.READ)) {
			sbc.position(getArchiveMetadataSize());
			while (sbc.position() < sbc.size()) {
				String header = Headers.retrieve(sbc);
				long fileSize = Headers.getFileSize(header);
				ArchEntityInfo info = new ArchEntityInfo(sbc.position(), header);
				Path p = Path.of(Headers.getRelativePath(header));
				cachedInfo.put(p, info);
				hierarchy.addChild(p);
				if (p.getNameCount() == 1) root = p;
				sbc.position(sbc.position() + header.length() + fileSize);
			}
		} catch (InvalidPathException exception) {
			throw new ArchiveCorruptedException(
				"File header corrupted: the path is unresolved: " + exception.getMessage(),
				exception
			);
		}
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

	private int getArchiveMetadataSize() {
		return (Metadata.getMetadata().toString() + "\n").length();
	}
}
