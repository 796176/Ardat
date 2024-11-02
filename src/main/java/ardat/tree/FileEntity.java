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
import java.nio.channels.ByteChannel;
import java.util.Arrays;

public class FileEntity extends ArchiveEntity{

	private final String localName;

	private final ByteChannel content;

	private final ArchiveEntityProperty[] pts;

	public FileEntity(String name, ByteChannel channel, ArchiveEntityProperty[] properties) {
		assert name != null && channel != null && properties != null;

		localName = name;
		content = channel;
		pts = new ArchiveEntityProperty[properties.length + 1];
		pts[0] = new ArchiveEntityProperty("class", getClass().getSimpleName());
		System.arraycopy(properties, 0, pts, 1, properties.length);
	}

	@Override
	public ArchiveEntity[] addChildren(ArchiveEntity... entity) {
		if (entity != null && entity.length > 0)
			throw new UnsupportedOperationException();
		return entity;
	}

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
	public String[] getName() {
		String[] parentName = new String[0];
		if (getParent() != null) parentName = getParent().getName();
		String[] name = Arrays.copyOf(parentName, parentName.length + 1);
		name[name.length - 1] = localName;
		return name;
	}
}