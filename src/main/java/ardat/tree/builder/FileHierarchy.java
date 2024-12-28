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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

/**
 * FileHierarchy is a class to provide instantaneous access to the child entities of the parent entity. All entities are
 * path objects. FileHierarchy uses a hash map to store the entities.<br><br>
 *
 * The access complexity is O(1).
 * The amortized insert complexity is O(1). The worst case insert complexity is O(n).<br><br>
 *
 * For example for given paths: /a, /a/b, /a/b/c; parent /a has a child /a/b and parent /a/b has a child /a/b/c.<br>
 * The time and result of adding those three paths will be the same as adding just /a/b/c because this will create
 * /a and /a/b recursively because they are parent directories of /a/b/c.
 */
public class FileHierarchy {

	private final HashMap<Path, Path[]> hierarchy;

	/**
	 * Constructs FileHierarchy with the initial capacity of {@link HashMap#HashMap()}.
	 */
	public FileHierarchy() {
		hierarchy = new HashMap<>();
	}

	/**
	 * Constructs FileHierarchy with the given capacity.
	 * @param capacity the capacity of the underlying HashMap
	 */
	public FileHierarchy(int capacity) {
		assert capacity >= 0;

		hierarchy = new HashMap<>(capacity);
	}

	/**
	 * Adds a new path entity. If the entity isn't a root and the parent entities haven't been added yet, adds them
	 * recursively. If the entity was already added does nothing.
	 * @param child a new entity to add
	 */
	public void addChild(Path child) {
		assert child != null;

		if (hierarchy.containsKey(child)) return;
		if (child.getNameCount() == 1 && !hierarchy.containsKey(child)) {
			hierarchy.put(child, new Path[0]);
			return;
		}
		if (!hierarchy.containsKey(child.getParent())) {
			addChild(child.getParent());
		}
		Path[] oldChildren = hierarchy.get(child.getParent());
		Path[] newChildren = Arrays.copyOf(oldChildren, oldChildren.length + 1);
		newChildren[newChildren.length - 1] = child;
		hierarchy.replace(child.getParent(), newChildren);
		hierarchy.put(child, new Path[0]);
	}

	/**
	 * Returns the child entities of the given parent entity.
	 * @param parent the parent entity
	 * @return the child entities if the parent entity was added, otherwise returns null
	 */
	public Path[] getChildren(Path parent) {
		assert parent != null;

		return hierarchy.get(parent);
	}
}
