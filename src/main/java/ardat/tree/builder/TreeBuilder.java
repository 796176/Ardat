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

public abstract class TreeBuilder {
	private final LinkedBlockingQueue<ArchiveEntity> queue = new LinkedBlockingQueue<>();

	protected abstract ArchiveEntity getRoot() throws IOException;

	protected abstract ArchiveEntity[] getChildren(ArchiveEntity entity) throws IOException;

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
