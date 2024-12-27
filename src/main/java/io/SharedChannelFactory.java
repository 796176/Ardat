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
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

/**
 * SharedChannelFactory is a global object intended to initialize and store the instances of
 * {@link SharedSeekableByteChannel}.
 */
public final class SharedChannelFactory {

	HashMap<Path, SeekableByteChannel> openedChannel = new HashMap<>(256);
	HashMap<SeekableByteChannel, Path> channelPathAssociation = new HashMap<>(256);
	HashMap<Path, Integer> counters = new HashMap<>(256);

	private static final SharedChannelFactory instance = new SharedChannelFactory();

	private SharedChannelFactory() {}

	/**
	 * Returns the SharedChannelFactory object.
	 * @return the SharedChannelFactory object
	 */
	public static SharedChannelFactory getSharedChannelFactory() {
		return instance;
	}

	/**
	 * Initialize a new instance of {@link SharedSeekableByteChannel} with {@link java.nio.channels.FileChannel} as an
	 * underlying channel.
	 * @param p the path to the file
	 * @param startingPos the index of the first available byte
	 * @param size the size of the available window
	 * @return an instance of SharedSeekableByteChannel
	 * @throws IOException if some I/O exceptions occur
	 */
	public SharedSeekableByteChannel newChannel(Path p, long startingPos, long size) throws IOException {
		assert p != null && startingPos >= 0 && startingPos + size <= Files.size(p);

		if (openedChannel.containsKey(p)) {
			counters.replace(p, counters.get(p) + 1);
			return new SharedSeekableByteChannel(openedChannel.get(p), startingPos, size);
		} else {
			SeekableByteChannel underlyingChannel = Files.newByteChannel(p, StandardOpenOption.READ);
			channelPathAssociation.put(underlyingChannel, p);
			SharedSeekableByteChannel newChannel =
				new SharedSeekableByteChannel(underlyingChannel, startingPos, size);
			openedChannel.put(p, underlyingChannel);
			counters.put(p, 1);
			return newChannel;
		}
	}

	/**
	 * The invocation is analogous to newChannel(p, startingPos, fileSize - staringPos).
	 */
	public SharedSeekableByteChannel newChannel(Path p, long startingPos) throws IOException {
		return newChannel(p, startingPos, Files.size(p) - startingPos);
	}

	/**
	 * The invocation is analogous to newChannel(p, 0, fileSize).
	 */
	public SharedSeekableByteChannel newChannel(Path p) throws IOException {
		return newChannel(p, 0, Files.size(p));
	}

	/**
	 * Initialize a new instance of {@link SharedSeekableByteChannel} with the same chanel as in the provided
	 * {@link SharedSeekableByteChannel} instance.
	 * @param channel the chanel to share the same underlying channel with
	 * @param startingPos the index of the first available byte
	 * @param size the size of the available window
	 * @return an instance of SharedSeekableByteChannel
	 * @throws IOException if some I/O exceptions occur
	 */
	public SharedSeekableByteChannel newChannel(
		SharedSeekableByteChannel channel,
		long startingPos,
		long size
	) throws IOException {
		assert channel != null && startingPos >= 0 && startingPos + size <= channel.getUnderlyingChannel().size();

		Path p = channelPathAssociation.get(channel.getUnderlyingChannel());
		counters.replace(p, counters.get(p) + 1);
		return new SharedSeekableByteChannel(channel.getUnderlyingChannel(), startingPos, size);
	}

	/**
	 * The invocation is analogous to newChannel(channel, startingPos, channel.size()).
	 */
	public SharedSeekableByteChannel newChannel(
		SharedSeekableByteChannel channel,
		long startingPos
	) throws IOException {
		return newChannel(channel, startingPos, channel.size());
	}

	/**
	 * The invocation is analogous to newChannel(channel, channel.getStart, channel.size()).
	 */
	public SharedSeekableByteChannel newChannel(SharedSeekableByteChannel channel) throws IOException {
		return newChannel(channel, channel.getStart(), channel.size());
	}

	/**
	 * Invoked by an instance of {@link SharedSeekableByteChannel} to notify that close() was invoked on that channel.
	 * @param channel the channel itself
	 */
	void notifyClosing(SharedSeekableByteChannel channel) {
		assert channel != null;

		Path p = channelPathAssociation.get(channel.getUnderlyingChannel());
		int counter = counters.get(p) - 1;
		if (counter == 0) {
			try {
				counters.remove(p);
				channelPathAssociation.remove(channel.getUnderlyingChannel());
				openedChannel.remove(p).close();
			} catch (IOException ignored) { }
		} else {
			counters.replace(p, counters.get(p) + 1);
		}
	}
}
