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
import java.nio.file.Path;

public class ArchiveTreeBuilderTests extends TreeBuilderTests implements TreeBuilderTestIterator {
	static ArchiveTreeBuilder[] builders;
	static ArchiveEntity[] results;
	static int counter = 0;

	@BeforeAll
	static void beforeAll() throws IOException {
		results = new ArchiveEntity[3];
		builders = new ArchiveTreeBuilder[3];

		String preGeneratedArchs =
			Path.of(System.getProperty("user.dir"), "src/test/resources/test_archives").toString();
		results[counter] = mkdir("dir1");
		builders[counter] =
			ArchiveTreeBuilder.getArchiveTreeBuilder(Path.of(preGeneratedArchs, "dir1_linux.ardat"));
		counter++;

		results[counter] = touch("file1", "qwerty".getBytes());
		builders[counter] =
			ArchiveTreeBuilder.getArchiveTreeBuilder(Path.of(preGeneratedArchs, "file1_linux.ardat"));
		counter++;

		results[counter] = mkdir("chain1");
		DirectoryEntity subDir = mkdir("subDir");
		results[counter].addChildren(subDir);
		FileEntity subFile = touch("subFile", "phoenix".getBytes());
		subDir.addChildren(subFile);
		builders[counter] =
			ArchiveTreeBuilder.getArchiveTreeBuilder(Path.of(preGeneratedArchs, "chain1_linux.ardat"));

		counter = 0;
	}

	private static DirectoryEntity mkdir(String name) {
		return new DirectoryEntity(
			name,
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "1"),
				new ArchiveEntityProperty("modify-time", "1"),
				new ArchiveEntityProperty("access-time", "1")
			}
		);
	}

	private static FileEntity touch(String name, byte[] content) {
		return new FileEntity(
			name,
			new ArraySeekableByteChannel(content),
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "1"),
				new ArchiveEntityProperty("modify-time", "1"),
				new ArchiveEntityProperty("access-time", "1")
			}
		);
	}

	@Override
	TreeBuilderTestIterator getIterator() {
		return this;
	}

	@Override
	public boolean next() {
		counter++;
		return builders.length > counter;
	}

	@Override
	public TreeBuilder getBuilder() {
		return builders[counter];
	}

	@Override
	public ArchiveEntity getExpectedResult() {
		return results[counter];
	}
}
