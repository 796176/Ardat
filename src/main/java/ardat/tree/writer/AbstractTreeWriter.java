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
import ardat.tree.root.TreeRoot;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * AbstractTreeWriter is an abstract class for performing the output operation on the entity tree.<br>
 * {@link FSTreeWriter} performs the output operation into the file system.<br>
 * {@link ArchiveEntity} performs the output operation into the archive file.
 */
public abstract class AbstractTreeWriter {

	private final LinkedBlockingQueue<ArchiveEntity> queue = new LinkedBlockingQueue<>();

	/**
	 * Passes the entity to the concrete file to perform the output operation.
	 * @param entity the passed entity
	 * @throws IOException if some I/O errors occur
	 */
	protected abstract void writeArchiveEntity(ArchiveEntity entity) throws IOException;

	/**
	 * Performs the output operation on the tree received from {@link TreeRoot}. The starting point is the root of the
	 * tree. After the root is processed, the methods processes the remaining tree elements wide-wise.
	 * @throws IOException if some I/O error occur
	 */
	public final void write() throws IOException {
		ArchiveEntity root = TreeRoot.getTreeRoot().get();
		assert root != null;

		queue.add(root);
		while (!queue.isEmpty()) {
			ArchiveEntity currentEntity = queue.poll();
			writeArchiveEntity(currentEntity);
			queue.addAll(Arrays.stream(currentEntity.getChildren()).toList());
		}
	}
}
