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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FSTreeWriterTests extends AbstractTreeWriterTests {
	Path workingDir;

	@BeforeEach
	void beforeEach() throws IOException {
		workingDir = Files.createTempDirectory(null);
	}
	@Override
	AbstractTreeWriter getWriter() throws IOException {
		return FSTreeWriter.getFSTreeWriter(workingDir);
	}

	@Override
	boolean isExpected(ArchiveEntity root) throws IOException {
		Path resultRootPath = Path.of(workingDir.toString(), root.getName()[0]);
		Path expectedRootPath = Path.of(
			System.getProperty("user.dir"),
			"src/test/resources/test_directories",
			root.getName()[0]
		);

		FileHierarchyContentComparator comparator =
			new FileHierarchyContentComparator(resultRootPath, expectedRootPath);
		Files.walkFileTree(resultRootPath, comparator);
		return !comparator.isTerminated();
	}

	@AfterEach
	void afterEach() throws IOException {
		Files.walkFileTree(workingDir, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
				Files.delete(path);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
				Files.delete(path);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static class FileHierarchyContentComparator extends SimpleFileVisitor<Path> {
		private FileVisitResult result = FileVisitResult.CONTINUE;
		private static Path visitorRoot;
		private static Path correspondingRoot;

		private FileHierarchyContentComparator(Path visitingRoot, Path correspondingRoot) {
			visitorRoot = visitingRoot;
			FileHierarchyContentComparator.correspondingRoot = correspondingRoot;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
			Path correspondingPath = Path.of(correspondingRoot.toString(), visitorRoot.relativize(path).toString());
			if (
				!Files.isDirectory(correspondingPath) &&
					attrs.lastModifiedTime().toMillis() != 1
			) {
				return result = FileVisitResult.TERMINATE;
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
			Path correspondingPath = Path.of(correspondingRoot.toString(), visitorRoot.relativize(path).toString());
			if (
				!Files.isRegularFile(correspondingPath) &&
					attrs.lastModifiedTime().toMillis() != 1
			) {
				return result = FileVisitResult.TERMINATE;
			}

			ByteBuffer buffer1 = ByteBuffer.allocate(1024);
			ByteBuffer buffer2 = ByteBuffer.allocate(1024);
			try (
				ByteChannel bc1 = Files.newByteChannel(path);
				ByteChannel bc2 = Files.newByteChannel(correspondingPath)) {
				while (bc1.read(buffer1) > 0 && bc2.read(buffer2) > 0) {
					buffer1.flip();
					buffer2.flip();
					if (!buffer1.equals(buffer2)) {
						return result = FileVisitResult.TERMINATE;
					}
					buffer1.clear();
					buffer2.clear();
				}
				if (buffer1.position() != buffer2.position()) {
					return result = FileVisitResult.TERMINATE;
				}
			}
			return FileVisitResult.CONTINUE;
		}

		public boolean isTerminated() {
			return result == FileVisitResult.TERMINATE;
		}
	}
}
