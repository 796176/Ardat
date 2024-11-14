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
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

public class FSTreeBuilderTests extends TreeBuilderTests implements TreeBuilderTestIterator {
	static FSTreeBuilder[] builders;
	static ArchiveEntity[] results;
	static int currentIndex = 0;

	@BeforeAll
	static void beforeAll() throws IOException {
		builders = new FSTreeBuilder[3];
		results = new ArchiveEntity[3];

		String preGeneratedDirs =
			Path.of(System.getProperty("user.dir"), "src/test/resources/test_directories").toString();
		Path singleFile = Path.of(preGeneratedDirs, "file1");
		builders[currentIndex] = new FSTreeBuilder(singleFile);
		BasicFileAttributes singleFileA =
			Files.getFileAttributeView(singleFile, BasicFileAttributeView.class).readAttributes();
		results[currentIndex] = touch("file1", "kingdom".getBytes(), singleFileA);
		currentIndex++;

		Path singleDir = Path.of(preGeneratedDirs, "dir1");
		builders[currentIndex] = new FSTreeBuilder(singleDir);
		BasicFileAttributes singleDirA =
			Files.getFileAttributeView(singleDir, BasicFileAttributeView.class).readAttributes();
		results[currentIndex] = mkdir("dir1", singleDirA);
		currentIndex++;

		Path complexTree = Path.of(preGeneratedDirs, "tree1");
		builders[currentIndex] = new FSTreeBuilder(complexTree);
		BasicFileAttributes complexTreeA =
			Files.getFileAttributeView(complexTree, BasicFileAttributeView.class).readAttributes();
		results[currentIndex] = mkdir("tree1", complexTreeA);
		BasicFileAttributes subDirA =
			Files
				.getFileAttributeView(Path.of(complexTree.toString(), "subDir"), BasicFileAttributeView.class)
				.readAttributes();
		DirectoryEntity subDir = mkdir("subDir", subDirA);
		BasicFileAttributes subFileA =
			Files
				.getFileAttributeView(Path.of(complexTree.toString(), "subFile"), BasicFileAttributeView.class)
				.readAttributes();
		FileEntity subFile = touch("subFile", "federacy".getBytes(), subFileA);
		results[currentIndex].addChildren(subDir, subFile);

		currentIndex = 0;
	}


	private static DirectoryEntity mkdir(String name, BasicFileAttributes attrs) {
		return new DirectoryEntity(
			name,
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "" + attrs.creationTime().toMillis()),
				new ArchiveEntityProperty("modify-time", "" + attrs.lastModifiedTime().toMillis()),
				new ArchiveEntityProperty("access-time", "" + attrs.lastAccessTime().toMillis())
			});
	}

	private static FileEntity touch(String name, byte[] c, BasicFileAttributes attrs) {
		return new FileEntity(
			name,
			new ArraySeekableByteChannel(c),
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "" + attrs.creationTime().toMillis()),
				new ArchiveEntityProperty("modify-time", "" + attrs.lastModifiedTime().toMillis()),
				new ArchiveEntityProperty("access-time", "" + attrs.lastAccessTime().toMillis())
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
}
