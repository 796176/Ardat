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
import java.nio.file.attribute.FileTime;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * FSTreeWriter is a concrete implementation of {@link AbstractTreeWriter} that performs the output operation on
 * the given tree into the file system.
 */
public class FSTreeWriter extends AbstractTreeWriter{

	private final Path dst;

	private final LinkedBlockingDeque<ArchiveEntity> branchEntities = new LinkedBlockingDeque<>();

	private long entitiesLeft = 1;

	private FSTreeWriter(Path destination) {
		assert destination != null;

		dst = destination;
	}

	/**
	 * Constructs a new instance of FSTreeWriter. Throws an IOException if the given destination is not a directory.
	 * @param destination the destination of the resulted file or directory
	 * @return an instance of FSTreeWriter
	 * @throws IOException if destination is not a directory
	 */
	public static FSTreeWriter getFSTreeWriter(Path destination) throws IOException {
		assert destination != null;
		if (!Files.isDirectory(destination)) throw new IOException("The " + destination + " path is not a directory");

		return new FSTreeWriter(destination);
	}

	@Override
	protected void writeArchiveEntity(ArchiveEntity entity) throws IOException {
		assert entity != null;

		entitiesLeft -= 1;
		Path entityPath = Path.of(dst.toString(), entity.getName());
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

		if (entity.getChildren().length == 0) {
			try {
				ArchiveEntityProperty[] pts = entity.getProperties();
				BasicFileAttributeView bfav = Files.getFileAttributeView(entityPath, BasicFileAttributeView.class);
				bfav.setTimes(
					FileTime.fromMillis(Long.parseLong(pts[find("modify-time", pts)].val())),
					FileTime.fromMillis(Long.parseLong(pts[find("access-time", pts)].val())),
					FileTime.fromMillis(Long.parseLong(pts[find("create-time", pts)].val()))
				);
				if (entitiesLeft == 0) {
					processBranches();
				}
			} catch (ArrayIndexOutOfBoundsException ignored) {
			}
		} else {
			branchEntities.push(entity);
			entitiesLeft += entity.getChildren().length;
		}
	}

	private int find(String key, ArchiveEntityProperty[] pts) {
		int index = 0;
		while (!pts[index].key().equals(key) && ++index < pts.length);
		if (index == pts.length) return -1;
		return index;
	}
	
	private void processBranches() throws IOException {
		for (ArchiveEntity dir : branchEntities) {
			Path dirPath = Path.of(dst.toString(), dir.getName());
			ArchiveEntityProperty[] pts = dir.getProperties();
			BasicFileAttributeView bfav = Files.getFileAttributeView(dirPath, BasicFileAttributeView.class);
			bfav.setTimes(
				FileTime.fromMillis(Long.parseLong(pts[find("modify-time", pts)].val())),
				FileTime.fromMillis(Long.parseLong(pts[find("access-time", pts)].val())),
				FileTime.fromMillis(Long.parseLong(pts[find("create-time", pts)].val()))
			);
		}
	}
}
