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

public class FileHierarchy {

	private final HashMap<Path, Path[]> hierarchy;

	public FileHierarchy() {
		hierarchy = new HashMap<>();
	}

	public FileHierarchy(int capacity) {
		assert capacity >= 0;

		hierarchy = new HashMap<>(capacity);
	}

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
	
	public Path[] getChildren(Path parent) {
		assert parent != null;

		return hierarchy.get(parent);
	}
}
