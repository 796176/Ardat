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

/**
 * ArchiveEntityConstructorInterface is a common interface that constructs an {@link ArchiveEntity} object.<br>
 * Of course, because {@link ArchiveEntity} is an abstract class the returned instance is a concrete implementation of
 * that class. The interface is used when the client knows the argument(s) to create an {@link ArchiveEntity} object,
 * but they don't know or simply don't care what concrete implementation actually takes this/these argument(s).<br><br>
 *
 * {@link ardat.tree.builder.archive.ArchivedEntityConstructor} is a good demonstration of this interface's purpose.
 * It takes an archived header as an argument and constructs a file, a directory, or a decorated file entity. The client
 * needn't check the header to pass it to the concrete implementation's constructor:
 * {@link ardat.tree.builder.archive.ArchivedEntityConstructor} does it in their stead.
 *
 * @param <T> parameter to construct an ArchiveEntity object
 */
public interface ArchiveEntityConstructorInterface<T> {
	/**
	 * Returns an {@link ArchiveEntity} using the provided parameter as a reference.
	 * @param input the parameter
	 * @return the constructed entity
	 */
	ArchiveEntity construct(T input);
}
