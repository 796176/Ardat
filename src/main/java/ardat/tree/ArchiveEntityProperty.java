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

import ardat.exceptions.ArchiveCorruptedException;

/**
 * ArchiveEntityProperty is a record to store an entity property.
 * @param key the key of the property
 * @param val the value of the property
 */
public record ArchiveEntityProperty (String key, String val){

	/**
	 * Searches for a value in the property array using a given key.
	 * @param key the key
	 * @param pts the property array
	 * @return the value
	 */
	public static String findVal(String key, ArchiveEntityProperty[] pts) {
		for (ArchiveEntityProperty pt: pts) {
			if (pt.key().equals(key))
				return pt.val();
		}
		throw new ArchiveCorruptedException("Property " + key + " isn't found");
	}
}
