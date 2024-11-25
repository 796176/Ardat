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

public class PrettyEntity extends ArchiveEntityProcessor {

	public PrettyEntity(ArchiveEntity archiveEntity, boolean encode) {
		assert archiveEntity != null;

		setComponent(archiveEntity);
		setEncode(encode);
	}

	public PrettyEntity(ArchiveEntity archiveEntity) {
		this(archiveEntity, true);
	}

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
			out.put(Byte.valueOf(new String(twoBytes), 16));
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
