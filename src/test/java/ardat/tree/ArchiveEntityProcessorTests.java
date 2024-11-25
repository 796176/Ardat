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

import ardat.tree.builder.ArraySeekableByteChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ArchiveEntityProcessorTests {

	abstract ArchiveEntityProcessor getInstance(ArchiveEntity archiveEntity);

	abstract ByteBuffer getEncodedData();

	abstract ByteBuffer getDecodedData();

	@Test
	void encodeTest() throws IOException {
		SeekableByteChannel channel = new ArraySeekableByteChannel(getDecodedData().array());
		ArchiveEntity fileEntity = new FileEntity("file", channel, new ArchiveEntityProperty[]{});
		ArchiveEntityProcessor encoder = getInstance(fileEntity);
		encoder.setEncode(true);

		ByteBuffer expectedOutput = getEncodedData();
		ByteBuffer actualOutput = ByteBuffer.allocate(expectedOutput.capacity());
		encoder.getContent(actualOutput);
		actualOutput.position(0);
		assertEquals(expectedOutput, actualOutput, "The encoded data doesn't match");
		assertFalse(
			encoder.hasRemainingContent(),
			"ArchiveEntityProcessor produced superfluous amount of data while encoding"
		);
	}

	@Test
	void decodeTest() throws IOException {
		SeekableByteChannel channel = new ArraySeekableByteChannel(getEncodedData().array());
		ArchiveEntity fileEntity = new FileEntity("file", channel, new ArchiveEntityProperty[]{});
		ArchiveEntityProcessor decoder = getInstance(fileEntity);
		decoder.setEncode(false);

		ByteBuffer expectedOutput = getDecodedData();
		ByteBuffer actualOutput = ByteBuffer.allocate(expectedOutput.capacity());
		decoder.getContent(actualOutput);
		actualOutput.position(0);
		assertEquals(expectedOutput, actualOutput, "The decoded data doesn't match");
		assertFalse(
			decoder.hasRemainingContent(),
			"ArchiveEntityProcessor produced superfluous amount of data while decoding"
		);
	}

}
