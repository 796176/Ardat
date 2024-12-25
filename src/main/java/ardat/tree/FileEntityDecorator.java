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

import java.util.LinkedList;

/**
 * FileEntityDecorator is a class that decorates the given {@link FileEntity} using the decorators in the list.
 */
public class FileEntityDecorator implements ArchiveEntityConstructorInterface<ArchiveEntity> {

	private final LinkedList<ArchiveEntityConstructorInterface<ArchiveEntity>> decorators = new LinkedList<>();

	/**
	 * Returns the list containing all the decorators. The changes made in the returned instance are applied to the
	 * local list.
	 * @return the list of decorators
	 */
	public LinkedList<ArchiveEntityConstructorInterface<ArchiveEntity>> getDecoratorList() {
		return decorators;
	}

	/**
	 * Encapsulates FileEntity using the list of decorators in order starting with the first one.
	 * @param input the archive entity
	 * @return the decorated FileEntity, otherwise input itself
	 */
	@Override
	public ArchiveEntity construct(ArchiveEntity input) {
		if (input instanceof FileEntity) {
			ArchiveEntity outerEntity = input;
			for (ArchiveEntityConstructorInterface<ArchiveEntity> constructor : getDecoratorList()) {
				outerEntity = constructor.construct(outerEntity);
			}
			return outerEntity;
		}

		return input;
	}
}
