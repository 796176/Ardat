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
import ardat.tree.DirectoryEntity;
import ardat.tree.FileEntity;
import ardat.tree.builder.ArraySeekableByteChannel;
import ardat.tree.root.TreeRoot;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractTreeWriterTests {

	abstract AbstractTreeWriter getWriter() throws IOException;

	abstract boolean isExpected(ArchiveEntity root) throws IOException;

	static Stream<ArchiveEntity> treeProvider() {
		LinkedList<ArchiveEntity> roots = new LinkedList<>();

		DirectoryEntity singleDir = mkdir("dir1");
		roots.add(singleDir);

		FileEntity singleFile = touch("file1", "qwerty".getBytes());
		roots.add(singleFile);

		DirectoryEntity tree1 = mkdir("tree1");
		DirectoryEntity tree1SubDir = mkdir("subDir");
		FileEntity tree1SubFile =  touch("subFile", "extraterrestrial".getBytes());
		tree1.addChildren(tree1SubDir, tree1SubFile);
		roots.add(tree1);

		DirectoryEntity chain1 = mkdir("chain1");
		DirectoryEntity chain1SubDir = mkdir("subDir");
		FileEntity chain1SubFile = touch("subFile", "phoenix".getBytes());
		chain1SubDir.addChildren(chain1SubFile);
		chain1.addChildren(chain1SubDir);
		roots.add(chain1);

		return roots.stream();
	}

	@ParameterizedTest
	@MethodSource("treeProvider")
	void writerTest(ArchiveEntity root) throws IOException {
		TreeRoot.getTreeRoot().set(root);
		getWriter().write();
		assertTrue(isExpected(root));
	}

	static DirectoryEntity mkdir(String name) {
		return new DirectoryEntity(
			name,
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "1"),
				new ArchiveEntityProperty("modify-time", "1"),
				new ArchiveEntityProperty("access-time", "1")
			}
		);
	}

	static FileEntity touch(String name, byte[] content) {
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
}
