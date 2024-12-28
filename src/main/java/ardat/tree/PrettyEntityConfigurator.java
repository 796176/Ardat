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
 * PrettyEntityConfigurator is a class to instantiate {@link PrettyEntity}. The class separates the configuration of
 * {@link PrettyEntity} and its actual instantiation, which happens in {@link #construct(ArchiveEntity)}.
 */
public class PrettyEntityConfigurator implements ArchiveEntityConstructorInterface<ArchiveEntity> {

	private boolean encode = true;

	/**
	 * Constructs PrettyEntityConfigurator.
	 * @param encode the encode field of PrettyEntity
	 */
	public PrettyEntityConfigurator(boolean encode) {
		this.encode = encode;
	}

	/**
	 * Constructs PrettyEntityConfigurator setting the encode field of PrettyEntity as true.
	 */
	public PrettyEntityConfigurator() {}

	/**
	 * Returns PrettyEntity.
	 * @param input an archive entity
	 * @return PrettyEntity
	 */
	@Override
	public ArchiveEntity construct(ArchiveEntity input) {
		return new PrettyEntity(input, encode);
	}
}
