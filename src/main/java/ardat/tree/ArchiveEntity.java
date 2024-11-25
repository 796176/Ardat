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

/**
 * ArchiveEntity is an abstract class, which stores information about a file descriptor.
 * ArchiveEntity is a component participant of the Composite pattern. Every ArchiveEntity object can have children or
 * a parent. If an ArchiveEntity doesn't have a parent it considered a root of the tree. If an ArchiveEntity can't have
 * children it considered a leaf.
 */
public abstract class ArchiveEntity implements Closeable {

	private ArchiveEntity parent;

	/**
	 * Sets a new parent of the current entity.
	 * @param entity a new parent
	 */
	protected final void setParent(ArchiveEntity entity) {
		parent = entity;
	}

	/**
	 * Returns the parent of the current entity.
	 * @return the parent of the current entity or null if it doesn't have one
	 */
	protected final ArchiveEntity getParent() {
		return parent;
	}

	/**
	 * Returns the children of the current entity organized in an array.
	 * @return the children of the current entity
	 */
	public ArchiveEntity[] getChildren() {
		return new ArchiveEntity[]{};
	}

	/**
	 * Adds new children to the current entity.
	 * @param entity children to be added
	 * @return the children that were added
	 */
	public abstract ArchiveEntity[] addChildren(ArchiveEntity... entity);

	/**
	 * Removes a child from the current entity.
	 * @param entity an entity to remove
	 * @return the removed entity
	 */
	public abstract ArchiveEntity removeChild(ArchiveEntity entity);

	/**
	 * Returns properties of the file descriptor.
	 * @return properties of the file descriptor
	 */
	public abstract ArchiveEntityProperty[] getProperties();

	/**
	 * Reads the content of the file descriptor.
	 * @param byteBuffer the buffer the content is written to
	 * @return the number of written bytes.
	 * @throws IOException if some I/O error occurs
	 */
	public int getContent(ByteBuffer byteBuffer) throws IOException {
		return 0;
	}

	/**
	 * Returns false if the underlying stream reached the end.
	 * @return false if the underlying stream reached the end, otherwise true
	 * @throws IOException if some I/O errors occur
	 */
	public boolean hasRemainingContent() throws IOException {
		return false;
	}

	/**
	 * Returns the unique name of the current entity in the tree organized in an array.
	 * The array is composed of non-unique entity names represented as strings. The index represents the depth of each
	 * entity where the 0th index is the root of the tree.
	 * @return the unique name of the current entity in the tree
	 */
	public abstract String[] getName();

	/**
	 * Returns true if the entity doesn't support adding and removing children.
	 * @return true if the entity doesn't support children related methods, otherwise returns false
	 */
	public boolean isLeaf() {
		return true;
	}

	/**
	 * Returns true if the channel associated with the current entity is opened.
	 * @return true if the channel is opened, otherwise returns false
	 */
	public boolean isOpened() {
		return false;
	}
}
