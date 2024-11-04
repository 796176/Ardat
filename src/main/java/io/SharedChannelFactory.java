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

public final class SharedChannelFactory {

	HashMap<Path, SeekableByteChannel> openedChannel = new HashMap<>(256);
	HashMap<SeekableByteChannel, Path> channelPathAssociation = new HashMap<>(256);
	HashMap<Path, Integer> counters = new HashMap<>(256);

	private static final SharedChannelFactory instance = new SharedChannelFactory();

	private SharedChannelFactory() {}

	public static SharedChannelFactory getSharedChannelFactory() {
		return instance;
	}

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

	public SharedSeekableByteChannel newChannel(Path p, long startingPos) throws IOException {
		return newChannel(p, startingPos, Files.size(p) - startingPos);
	}

	public SharedSeekableByteChannel newChannel(Path p) throws IOException {
		return newChannel(p, 0, Files.size(p));
	}

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

	public SharedSeekableByteChannel newChannel(
		SharedSeekableByteChannel channel,
		long startingPos
	) throws IOException {
		return newChannel(channel, startingPos, channel.size());
	}

	public SharedSeekableByteChannel newChannel(SharedSeekableByteChannel channel) throws IOException {
		return newChannel(channel, channel.getStart(), channel.size());
	}

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
