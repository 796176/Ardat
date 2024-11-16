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

public class ArchiveTreeWriterTests extends AbstractTreeWriterTests {
	Path workingDir;
	Path resultArch;

	@BeforeEach
	void beforeEach() throws IOException {
		workingDir = Files.createTempDirectory(null);
		resultArch = Path.of(workingDir.toString(), "result.ardat");
	}
	@Override
	AbstractTreeWriter getWriter() throws IOException{
		return ArchiveTreeWriter.getArchiveTreeWriter(resultArch);
	}

	@Override
	boolean isExpected(ArchiveEntity root) throws IOException{
		Path expectedArch = Path.of(
			System.getProperty("user.dir"),
			"src/test/resources/test_archives",
			root.getName()[root.getName().length - 1] + "_" + System.getProperty("os.name").toLowerCase() + ".ardat");

		ByteBuffer buffer1 = ByteBuffer.allocate(1024);
		ByteBuffer buffer2 = ByteBuffer.allocate(1024);
		try (
			ByteChannel bc1 = Files.newByteChannel(resultArch, StandardOpenOption.READ);
			ByteChannel bc2 = Files.newByteChannel(expectedArch, StandardOpenOption.READ)
		) {
			while (bc1.read(buffer1) > 0 && bc2.read(buffer2) > 0) {
				buffer1.flip();
				buffer2.flip();
				if (!buffer1.equals(buffer2)) return false;
				buffer1.clear();
				buffer2.clear();
			}
			return buffer1.position() == buffer2.position();
		}
	}

	@AfterEach
	void afterEach() throws IOException{
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
}
