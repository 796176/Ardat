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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TreeBuilderTests {

	abstract TreeBuilderTestIterator getIterator();

	@Test
	void buildingTest() throws IOException {
		TreeBuilderTestIterator iterator = getIterator();
		int counter = 0;
		do {
			ArchiveEntity root = iterator.getBuilder().build().get();
			assertTrue(
				compareTrees(root, iterator.getExpectedResult()),
				"Test failed at iteration index " + counter
			);
			counter++;
		} while (iterator.next());
	}

	private boolean compareTrees(ArchiveEntity tree1, ArchiveEntity tree2) throws IOException {
		if (
			tree1.getClass().equals(tree2.getClass()) &&
			Arrays.equals(tree1.getName(), tree2.getName()) &&
			tree1.getChildren().length == tree2.getChildren().length &&
			Arrays.equals(tree1.getProperties(), tree2.getProperties())
		) {
			if (!compareContents(tree1, tree2)) return false;
			for (ArchiveEntity tree1Child: tree1.getChildren()) {
				ArchiveEntity tree2Child =
					Arrays
						.stream(tree2.getChildren())
						.filter(ae -> Arrays.equals(ae.getName(), tree1Child.getName()))
						.findAny()
						.get();
				if (!compareTrees(tree1Child, tree2Child)) return false;
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean compareContents(ArchiveEntity e1, ArchiveEntity e2) throws IOException {
		ByteBuffer buffer1 = ByteBuffer.allocate(1024);
		ByteBuffer buffer2 = ByteBuffer.allocate(1024);
		while (e1.getContent(buffer1) > 0 && e2.getContent(buffer2) > 0) {
			buffer1.flip();
			buffer2.flip();
			if (!buffer1.equals(buffer2)) return false;
			buffer1.clear();
			buffer2.clear();
		}
		return buffer1.position() == buffer2.position();
	}
}
