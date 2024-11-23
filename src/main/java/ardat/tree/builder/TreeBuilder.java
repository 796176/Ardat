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
import ardat.tree.root.TreeRoot;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TreeBuilder is an abstract class intended to create entity trees from different sources.<br>
 * {@link FSTreeBuilder} creates an entity tree using the file or the directory.<br>
 * {@link ArchiveEntity} creates an entity tree using the archive file.
 */
public abstract class TreeBuilder {
	private final LinkedBlockingQueue<ArchiveEntity> queue = new LinkedBlockingQueue<>();

	/**
	 * The concrete implementation constructs the root of the entity tree.
	 * @return the root of the entity tree
	 * @throws IOException if some I/O errors occur
	 */
	protected abstract ArchiveEntity getRoot() throws IOException;

	/**
	 * The concrete implementation constructs the children of the given entity.
	 * @param entity the parent entity
	 * @return an array of the children
	 * @throws IOException if some I/O errors occur
	 */
	protected abstract ArchiveEntity[] getChildren(ArchiveEntity entity) throws IOException;

	/**
	 * Constructs a new entity tree wide-wise starting with the root. Puts the root tree reference to
	 * the {@link TreeRoot} global object.
	 * @return the {@link TreeRoot} global object
	 * @throws IOException if some I/O errors occur
	 */
	public TreeRoot build() throws IOException {
		ArchiveEntity treeRoot = getRoot();
		queue.add(treeRoot);
		while (!queue.isEmpty()) {
			ArchiveEntity currentEntity = queue.poll();
			ArchiveEntity[] children = getChildren(currentEntity);
			queue.addAll(Arrays.stream(children).toList());
			currentEntity.addChildren(children);
		}
		TreeRoot.getTreeRoot().set(treeRoot);

		return TreeRoot.getTreeRoot();
	}
}
