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
import ardat.tree.ArchiveEntityProperty;
import ardat.tree.DirectoryEntity;
import ardat.tree.FileEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

public class FSTreeBuilderTests extends TreeBuilderTests implements TreeBuilderTestIterator{

	static Path workingDir;
	static FSTreeBuilder[] builders;
	static ArchiveEntity[] results;
	static int currentIndex = 0;

	@BeforeAll
	static void beforeAll() throws IOException {
		workingDir = Files.createTempDirectory(null);
		builders = new FSTreeBuilder[3];
		results = new ArchiveEntity[3];

		Path singleFile = Path.of(workingDir.toString(), "file1");
		builders[currentIndex] = new FSTreeBuilder(singleFile);
		results[currentIndex] = touch(singleFile, "content".getBytes());
		currentIndex++;

		Path singleDir = Path.of(workingDir.toString(), "dir1");
		builders[currentIndex] = new FSTreeBuilder(singleDir);
		results[currentIndex] = mkdir(singleDir);
		currentIndex++;

		Path complexTree = Path.of(workingDir.toString(), "tree1");
		builders[currentIndex] = new FSTreeBuilder(complexTree);
		results[currentIndex] = mkdir(complexTree);
		DirectoryEntity subDir = mkdir(Path.of(complexTree.toString(), "subDir"));
		FileEntity subFile = touch(Path.of(complexTree.toString(), "subFile"), "liberty".getBytes());
		results[currentIndex].addChildren(subDir, subFile);

		currentIndex = 0;
	}


	private static DirectoryEntity mkdir(Path p) throws IOException {
		Path dirPath = Files.createDirectory(p);
		BasicFileAttributes dirA = Files.getFileAttributeView(dirPath, BasicFileAttributeView.class).readAttributes();
		return new DirectoryEntity(
			p.getName(p.getNameCount() - 1).toString(),
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "" + dirA.creationTime().toMillis()),
				new ArchiveEntityProperty("modify-time", "" + dirA.lastModifiedTime().toMillis()),
				new ArchiveEntityProperty("access-time", "" + dirA.lastAccessTime().toMillis())
			});
	}

	private static FileEntity touch(Path p, byte[] c) throws IOException {
		Path filePath = Files.createFile(p);
		try (ByteChannel bc = Files.newByteChannel(filePath, StandardOpenOption.WRITE)) {
			bc.write(ByteBuffer.wrap(c));
		}
		BasicFileAttributes fileA = Files.getFileAttributeView(filePath, BasicFileAttributeView.class).readAttributes();
		return new FileEntity(
			p.getName(p.getNameCount() - 1).toString(),
			Files.newByteChannel(filePath, StandardOpenOption.READ),
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "" + fileA.creationTime().toMillis()),
				new ArchiveEntityProperty("modify-time", "" + fileA.lastModifiedTime().toMillis()),
				new ArchiveEntityProperty("access-time", "" + fileA.lastAccessTime().toMillis())
			});
	}

	@Override
	TreeBuilderTestIterator getIterator() {
		return this;
	}

	@Override
	public boolean next() {
		currentIndex++;
		return currentIndex < builders.length;
	}

	@Override
	public TreeBuilder getBuilder() {
		return builders[currentIndex];
	}

	@Override
	public ArchiveEntity getExpectedResult() {
		return results[currentIndex];
	}

	@AfterAll
	static void afterAll() throws IOException {
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

		for (ArchiveEntity ae: results) {
			ae.close();
		}
	}
}
