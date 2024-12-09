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

package crypto;

import java.util.HashSet;
import java.util.Random;

/**
 * IV128 is a global object to generate unique 128 bit vectors.
 */
public class IV128 {

	private final HashSet<byte[]> vectors = new HashSet<>();

	private static final IV128 instance = new IV128();

	private IV128() {}

	/**
	 * Returns the instance of IV128.
	 * @return the instance of IV128
	 */
	public static IV128 getIV128() {
		return instance;
	}

	/**
	 * Generates a unique vector which hasn't been generated before.
	 * @return a new vector
	 */
	public byte[] generate() {
		Random random = new Random(System.currentTimeMillis());
		byte[] newVector = new byte[16];
		do {
			random.nextBytes(newVector);
		} while (vectors.contains(newVector));
		vectors.add(newVector);
		return newVector;
	}
}