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

package ardat.exceptions;

/**
 * Thrown when the archive file semantics doesn't match that of {@link ardat.tree.writer.ArchiveTreeWriter}.
 */
public class ArchiveCorruptedException extends RuntimeException {

	/**
	 * Constructs an ArchiveCorruptedException with the specified detail message.
	 * @param message the specified message
	 */
	public ArchiveCorruptedException(String message) {
		super(message);
	}

	/**
	 * Constructs an ArchiveCorruptedException with no detail message.
	 */
	public ArchiveCorruptedException() {
		super();
	}

	/**
	 * Constructs an ArchiveCorruptedException with the specified detail message and cause.
	 * @param message the specified message
	 * @param cause the case
	 */
	public ArchiveCorruptedException(String message, Throwable cause) {
		super(message, cause);
	}
}
