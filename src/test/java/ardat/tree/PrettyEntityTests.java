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

public class PrettyEntityTests extends ArchiveEntityProcessorTests {
	@Override
	ArchiveEntityProcessor getInstance(ArchiveEntity archiveEntity) {
		return new PrettyEntity(archiveEntity);
	}

	@Override
	ByteBuffer getEncodedData() {
		return ByteBuffer.wrap("4865726520636f6d657320746865206368696c6c696e67207068617365".getBytes());
	}

	@Override
	ByteBuffer getDecodedData() {
		return ByteBuffer.wrap("Here comes the chilling phase".getBytes());
	}
}
