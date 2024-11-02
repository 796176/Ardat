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

package io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

public class SharedSeekableByteChannel implements SeekableByteChannel {

	private final SeekableByteChannel channel;

	private final long channelSize;

	private long offset;

	private long localSize;

	private long localPosition = 0;

	public SharedSeekableByteChannel(
		SeekableByteChannel seekableByteChannel,
		long startingPos,
		long size
	) throws IOException {
		assert seekableByteChannel != null &&
			startingPos >= 0 &&
			size >= 0 &&
			seekableByteChannel.size() >= size + startingPos;

		channel = seekableByteChannel;
		offset = startingPos;
		localSize = size;
		channelSize = channel.size();
	}

	public SharedSeekableByteChannel(SeekableByteChannel seekableByteChannel, long startingPos) throws IOException {
		this(seekableByteChannel, startingPos, seekableByteChannel.size() - startingPos);
	}

	@Override
	public int read(ByteBuffer byteBuffer) throws IOException {
		if (position() == size()) return -1;
		byteBuffer.limit((int) Math.min(
			byteBuffer.limit(),
			byteBuffer.position() + size() - position()
		));
		int result = channel.position(localPosition + offset).read(byteBuffer);
		localPosition += result;
		return result;
	}

	@Override
	public int write(ByteBuffer byteBuffer) {
		throw new NonWritableChannelException();
	}

	@Override
	public long position() {
		return localPosition;
	}

	@Override
	public SeekableByteChannel position(long l) {
		localPosition = l;
		return this;
	}

	@Override
	public long size() {
		return localSize;
	}

	@Override
	public SeekableByteChannel truncate(long l) {
		throw new NonWritableChannelException();
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void close() {
	}

	public long getStart() {
		return offset;
	}

	public void setRange(long startingPos, long size) {
		assert startingPos >= 0 && size >= 0 && channelSize >= size + startingPos;

		offset = startingPos;
		localSize = size;
		if (position() > size()) position(size);
	}

	public void setRange(long startingPos) {
		setRange(startingPos, Math.min(size(), channelSize - startingPos));
	}

	public void setSize(long size) {
		setRange(offset, size);
	}

	public SharedSeekableByteChannel cloneChannel() throws IOException {
		SharedSeekableByteChannel clonedSharedSeekableByteChannel =
			new SharedSeekableByteChannel(channel, offset, size());
		clonedSharedSeekableByteChannel.position(clonedSharedSeekableByteChannel.position());
		return clonedSharedSeekableByteChannel;
	}
}
