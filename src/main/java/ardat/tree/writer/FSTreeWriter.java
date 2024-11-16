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

package ardat.tree.writer;

import ardat.tree.ArchiveEntity;
import ardat.tree.ArchiveEntityProperty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class FSTreeWriter extends AbstractTreeWriter{

	private final Path dst;

	private FSTreeWriter(Path destination) {
		assert destination != null;

		dst = destination;
	}

	public static FSTreeWriter getFSTreeWriter(Path destination) throws IOException {
		assert destination != null;
		if (!Files.isDirectory(destination)) throw new IOException("The " + destination + " path is not a directory");

		return new FSTreeWriter(destination);
	}

	@Override
	protected void writeArchiveEntity(ArchiveEntity entity) throws IOException {
		assert entity != null;

		Path entityPath = Path.of(dst.toString(), entity.getName());
		Path entityPathParent = entityPath.getParent();
		BasicFileAttributeView residentialDirAttributeView =
			Files
				.getFileAttributeView(entityPathParent, BasicFileAttributeView.class);
		BasicFileAttributes residentialDirAttributes = residentialDirAttributeView.readAttributes();
		if (!entity.isLeaf()) {
			Files.createDirectory(entityPath);
		} else {
			try(
				WritableByteChannel wbc =
					Files.newByteChannel(entityPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
			) {
				ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);
				while (entity.getContent(byteBuffer) > 0) {
					byteBuffer.flip();
					wbc.write(byteBuffer);
					byteBuffer.clear();
				}
			}
		}

		try {
			ArchiveEntityProperty[] pts = entity.getProperties();
			BasicFileAttributeView bfav = Files.getFileAttributeView(entityPath, BasicFileAttributeView.class);
			bfav.setTimes(
				FileTime.fromMillis(Long.parseLong(pts[find("modify-time", pts)].val())),
				FileTime.fromMillis(Long.parseLong(pts[find("access-time", pts)].val())),
				FileTime.fromMillis(Long.parseLong(pts[find("create-time", pts)].val()))
			);
		} catch (ArrayIndexOutOfBoundsException ignored) { }

		residentialDirAttributeView.setTimes(
			residentialDirAttributes.lastModifiedTime(),
			residentialDirAttributes.lastAccessTime(),
			residentialDirAttributes.creationTime()
		);
	}

	private int find(String key, ArchiveEntityProperty[] pts) {
		int index = 0;
		while (!pts[index].key().equals(key) && ++index < pts.length);
		if (index == pts.length) return -1;
		return index;
	}
}
