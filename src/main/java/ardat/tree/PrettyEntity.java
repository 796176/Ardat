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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * PrettyEntity is a concrete implementation of {@link ArchiveEntityProcessor}, that renders the data of the underlying
 * entity as hexadecimal, meaning that every byte is encoded as 2 characters ranging from 00 to ff.
 */
public class PrettyEntity extends ArchiveEntityProcessor {

	/**
	 * Constructs PrettyEntity using the provided entity
	 * @param archiveEntity an archive entity
	 * @param encode an operation mode
	 */
	public PrettyEntity(ArchiveEntity archiveEntity, boolean encode) {
		assert archiveEntity != null;

		setComponent(archiveEntity);
		setEncode(encode);
	}

	/**
	 * Constructs PrettyEntity using the provided entity
	 * @param archiveEntity an archive entity
	 */
	public PrettyEntity(ArchiveEntity archiveEntity) {
		this(archiveEntity, true);
	}

	/**
	 * Returns properties of the underlying entity with the current properties attached to the end.
	 * @return properties of the underlying entity with the current properties attached to the end
	 */
	@Override
	public ArchiveEntityProperty[] getProperties() {
		ArchiveEntityProperty[] pts = Arrays.copyOf(super.getProperties(), super.getProperties().length + 1);
		pts[pts.length - 1] = new ArchiveEntityProperty("class", getClass().getSimpleName());
		return pts;
	}


	@Override
	protected int encode(ByteBuffer in, ByteBuffer out) {
		assert in != null && out != null;

		int oldOutPos = out.position();
		while (in.hasRemaining()) {
			byte b = in.get();
			String bString = HexFormat.of().toHexDigits(b);
			out.put(bString.getBytes());
		}

		return out.position() - oldOutPos;
	}

	@Override
	protected int decode(ByteBuffer in, ByteBuffer out) {
		assert in != null && out != null;

		int oldOutPos = out.position();
		while (in.hasRemaining()) {
			byte[] twoBytes = new byte[2];
			in.get(twoBytes);
			out.put(Integer.valueOf(new String(twoBytes), 16).byteValue());
		}
		return out.position() - oldOutPos;
	}

	@Override
	protected int getPreferredUnprocessedWindowSize() {
		return 1024;
	}

	@Override
	protected int getPreferredProcessedWindowSize() {
		return getPreferredUnprocessedWindowSize() * 2;
	}
}
