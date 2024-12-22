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

import java.security.Key;

/**
 * GlobalKey is a global object that stores an AES key to encrypt and decrypt data in {@link ardat.tree.AESEntity}.
 */
public class GlobalKey {

	private Key key;

	private static final GlobalKey instance = new GlobalKey();

	private GlobalKey() {}

	/**
	 * Returns the GlobalKey object.
	 * @return the GlobalKey object
	 */
	public static GlobalKey getGlobalKey() {
		return instance;
	}

	/**
	 * Sets a new key
	 * @param key a new key
	 */
	public void setKey(Key key) {
		this.key = key;
	}

	/**
	 * Returns the key.
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}
}
