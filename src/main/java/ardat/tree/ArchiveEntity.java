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

package ardat.tree;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ArchiveEntity implements Closeable {

	private ArchiveEntity parent;

	protected final void setParent(ArchiveEntity entity) {
		parent = entity;
	}

	protected final ArchiveEntity getParent() {
		return parent;
	}

	public ArchiveEntity[] getChildren() {
		return new ArchiveEntity[]{};
	}

	public abstract ArchiveEntity[] addChildren(ArchiveEntity... entity);

	public abstract ArchiveEntity removeChild(ArchiveEntity entity);

	public abstract ArchiveEntityProperty[] getProperties();

	public int getContent(ByteBuffer byteBuffer) throws IOException {
		return 0;
	}

	public abstract String[] getName();

	public boolean isLeaf() {
		return true;
	}

	public boolean isOpened() {
		return false;
	}
}
