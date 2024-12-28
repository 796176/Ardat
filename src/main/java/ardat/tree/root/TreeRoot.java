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

package ardat.tree.root;

import ardat.tree.ArchiveEntity;

import java.io.IOException;

/**
 * TreeRoot is a global object to store the tree of the archive entity.<br><br>
 * Every {@link ArchiveEntity} can have an open channel associated with it, so it's necessary to free the resources.
 * This job is delegated to TreeRoot, which stores only one reference.
 */
public class TreeRoot {

	private static final TreeRoot instance = new TreeRoot();

	private ArchiveEntity rootEntity = null;

	private TreeRoot() {}

	/**
	 * Returns the instance of TreeRoot.
	 * @return the instance of TreeRoot
	 */
	public static TreeRoot getTreeRoot() {
		return instance;
	}

	/**
	 * Returns the root of the entity tree.
	 * @return the root of the entity tree
	 */
	public ArchiveEntity get() {
		return rootEntity;
	}

	/**
	 * Sets a new root of the entity tree. Calls close() on the previous one.
	 * @param root a new root of the entity tree
	 */
	public void set(ArchiveEntity root) {
		try {
			if (rootEntity != null) rootEntity.close();
		} catch (IOException ignored) { }
		rootEntity = root;
	}
}
