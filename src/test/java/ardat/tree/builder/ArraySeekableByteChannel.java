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

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class ArraySeekableByteChannel implements SeekableByteChannel {

	private final byte[] arr;

	private long pos = 0;

	public ArraySeekableByteChannel(byte[] array) {
		assert array != null;

		arr = array;
	}
	@Override
	public int read(ByteBuffer byteBuffer) {
		assert byteBuffer != null;

		if (position() >= size()) return -1;
		int bytesToRead = (int) Math.min(byteBuffer.limit() - byteBuffer.position(), size() - position());
		byteBuffer.put(
			byteBuffer.position(),
			arr,
			(int) position(),
			bytesToRead
		);
		byteBuffer.position(byteBuffer.position() + bytesToRead);
		position(position() + bytesToRead);
		return bytesToRead;
	}

	@Override
	public int write(ByteBuffer byteBuffer) {
		return -1;
	}

	@Override
	public long position() {
		return pos;
	}

	@Override
	public SeekableByteChannel position(long l) {
		assert l >= 0;

		pos = Math.min(l, size());
		return this;
	}

	@Override
	public long size() {
		return arr.length;
	}

	@Override
	public SeekableByteChannel truncate(long l) {
		return null;
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() {}
}
