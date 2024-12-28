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

import java.nio.ByteBuffer;

public class Buffers {
	public static int transfer(ByteBuffer in, ByteBuffer out) {
		int transferred = Math.min(in.remaining(), out.remaining());
		int inLimit = in.limit();
		out.put(in.limit(transferred + in.position()));
		in.limit(inLimit);
		return transferred;
	}
}
