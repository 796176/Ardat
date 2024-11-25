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


import io.Buffers;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ArchiveEntityProcessor extends ArchiveEntityDecorator {

	private boolean encode = true;

	private final ByteBuffer read = ByteBuffer.allocate(getPreferredUnprocessedWindowSize());

	private final ByteBuffer processed = ByteBuffer.allocate(getPreferredProcessedWindowSize());

	protected abstract int encode(ByteBuffer in, ByteBuffer out);

	protected abstract int decode(ByteBuffer in, ByteBuffer out);

	protected abstract int getPreferredUnprocessedWindowSize();

	protected abstract int getPreferredProcessedWindowSize();

	public void setEncode(boolean encode) {
		this.encode = encode;
	}

	protected ArchiveEntityProcessor() {
		processed.flip();
	}

	private int process(ByteBuffer in, ByteBuffer out) {
		assert in != null && out != null;

		if (encode) return encode(in, out);
		else return decode(in, out);
	}

	@Override
	public int getContent(ByteBuffer byteBuffer) throws IOException {
		assert byteBuffer != null;

		int transferred = 0;
		if (processed.hasRemaining()) {
			transferred += Buffers.transfer(processed, byteBuffer);
			if (!byteBuffer.hasRemaining()) {
				return transferred;
			}
		}

		while (byteBuffer.hasRemaining() && super.getContent(read) > 0) {
			processed.clear();
			read.flip();
			process(read, processed);
			processed.flip();
			transferred += Buffers.transfer(processed, byteBuffer);
			read.clear();
		}

		if (transferred == 0 && !hasRemainingContent()) return -1;
		else return transferred;
	}

	@Override
	public boolean hasRemainingContent() throws IOException {
		return super.hasRemainingContent() || processed.hasRemaining();
	}

	@Override
	public void setComponent(ArchiveEntity archiveEntity) {
		assert archiveEntity != null;

		super.setComponent(archiveEntity);
	}
}
