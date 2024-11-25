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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

/**
 * FileEntity is a concrete implementation of {@link ArchiveEntity} that doesn't support children related operations.
 * FileEntity is a Leaf participant of the Composite pattern.
 */
public class FileEntity extends ArchiveEntity{

	private final String localName;

	private final SeekableByteChannel content;

	private final ArchiveEntityProperty[] pts;

	/**
	 * Constructs a FileEntity object given the local name, the channel containing the file content and the
	 * associated properties.
	 * @param name the local name
	 * @param channel the content of the file
	 * @param properties the associated properties
	 */
	public FileEntity(String name, SeekableByteChannel channel, ArchiveEntityProperty[] properties) {
		assert name != null && channel != null && properties != null;

		localName = name;
		content = channel;
		pts = new ArchiveEntityProperty[properties.length + 1];
		pts[0] = new ArchiveEntityProperty("class", getClass().getSimpleName());
		System.arraycopy(properties, 0, pts, 1, properties.length);
	}

	/**
	 * Attempts to add children throws {@link UnsupportedOperationException}.
	 */
	@Override
	public ArchiveEntity[] addChildren(ArchiveEntity... entity) {
		if (entity != null && entity.length > 0)
			throw new UnsupportedOperationException();
		return entity;
	}

	/**
	 * Attempts to add children throws {@link UnsupportedOperationException}.
	 */
	@Override
	public ArchiveEntity removeChild(ArchiveEntity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArchiveEntityProperty[] getProperties() {
		return pts;
	}

	@Override
	public int getContent(ByteBuffer byteBuffer) throws IOException {
		assert byteBuffer != null;

		return content.read(byteBuffer);
	}

	@Override
	public boolean hasRemainingContent() throws IOException {
		return content.position() < content.size();
	}

	@Override
	public String[] getName() {
		String[] parentName = new String[0];
		if (getParent() != null) parentName = getParent().getName();
		String[] name = Arrays.copyOf(parentName, parentName.length + 1);
		name[name.length - 1] = localName;
		return name;
	}

	/**
	 * Closes the channel associated with this entity.
	 * @throws IOException If some I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		content.close();
	}

	@Override
	public boolean isOpened() {
		return content.isOpen();
	}
}
