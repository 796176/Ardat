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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * SharedSeekableByteChannel is a wrapper class of {@link SeekableByteChannel}, which allows it to reference to the
 * same concrete implementation of {@link SeekableByteChannel} ( e.g. {@link java.nio.channels.FileChannel} ).<br><br>
 *
 * The instance of the class is accessible through the {@link SharedChannelFactory} global object. <br><br>
 * Due to the ability to share the same channel, SharedSeekableByteChannel doesn't support writing operations, it also
 * can't close the channel by itself delegating this operation to {@link SharedChannelFactory}.
 */
public class SharedSeekableByteChannel implements SeekableByteChannel {

	private final SeekableByteChannel channel;

	private final long channelSize;

	private long offset;

	private long localSize;

	private long localPosition = 0;

	private boolean isOpened = true;

	/**
	 * Constructs the object given the channel and the available range of accessible data.
	 * @param seekableByteChannel the channel to wrap
	 * @param startingPos the index of the first available byte
	 * @param size the size of the available window
	 * @throws IOException if some I/O errors occur
	 */
	SharedSeekableByteChannel(
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

	@Override
	public int read(ByteBuffer byteBuffer) throws IOException {
		if (!isOpened) throw new ClosedChannelException();
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
		return isOpened;
	}

	@Override
	public void close() {
		if (isOpened) {
			SharedChannelFactory.getSharedChannelFactory().notifyClosing(this);
			isOpened = false;
		}
	}

	/**
	 * Returns the index of the first byte available to read.
	 * @return the index of the first byte available to read
	 */
	public long getStart() {
		return offset;
	}

	/**
	 * Sets the available range of accessible data.
	 * @param startingPos the index of the first available byte
	 * @param size the size of the available window
	 */
	public void setRange(long startingPos, long size) {
		assert startingPos >= 0 && size >= 0 && channelSize >= size + startingPos;

		offset = startingPos;
		localSize = size;
		if (position() > size()) position(size);
	}

	/**
	 * The invocation is analogous to setRange(startingPos, size), where the size is contracted if necessary.
	 */
	public void setRange(long startingPos) {
		setRange(startingPos, Math.min(size(), channelSize - startingPos));
	}

	/**
	 * Sets a new size of the available window.
	 * @param size the size of the available window
	 */
	public void setSize(long size) {
		setRange(offset, size);
	}

	/**
	 * Returns the wrapped channel.
	 * @return the wrapped channel
	 */
	SeekableByteChannel getUnderlyingChannel() {
		return channel;
	}
}
