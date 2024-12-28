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

import crypto.*;
import io.GlobalKey;

import java.util.HexFormat;

/**
 * AESEntityConfigurator is used to simplify the instantiation of {@link AESEntity}. It generates all the necessary
 * parameters based on the provided strategy and retrieves the key using {@link GlobalKey#getKey()}. The class separates
 * the configuration of {@link AESEntity} and its actual instantiation, which happens in
 * {@link #construct(ArchiveEntity)}.
 */
public class AESEntityConfigurator implements ArchiveEntityConstructorInterface<ArchiveEntity> {

	private boolean encode = true;

	private Class<? extends AESStrategy> strategyClass;

	/**
	 * Constructs AESEntityConfigurator using the provided strategy and the encoding parameter.
	 * @param strategy an AESStrategy class
	 * @param encode the encode field of AESEntity
	 */
	public AESEntityConfigurator(Class<? extends AESStrategy> strategy, boolean encode) {
		assert strategyClass != null;

		setStrategy(strategy);
		this.encode = encode;
	}

	/**
	 * Constructs AESEntityConfigurator using the provided strategy and sets the encoding parameter to true.
	 * @param strategy an AESStrategy class
	 */
	public AESEntityConfigurator(Class<? extends AESStrategy> strategy) {
		assert strategyClass != null;

		setStrategy(strategy);
	}

	/**
	 * Returns AESEntity
	 * @param input an archive entity
	 * @return AESEntity
	 */
	@Override
	public ArchiveEntity construct(ArchiveEntity input) {
		ArchiveEntityProperty[] pts = null;
		if (strategyClass.equals(AESCBCStrategy.class)) {
			byte[] iv = IV128.getIV128().generate();
			pts = new ArchiveEntityProperty[] {
				new ArchiveEntityProperty("strategy", strategyClass.getName()),
				new ArchiveEntityProperty("iv128", HexFormat.of().formatHex(iv))
			};
		} else if (strategyClass.equals(AESCTRStrategy.class)) {
			byte[] iv = IV64.getIV64().generate();
			pts = new ArchiveEntityProperty[] {
				new ArchiveEntityProperty("strategy", strategyClass.getName()),
				new ArchiveEntityProperty("iv64", HexFormat.of().formatHex(iv))
			};
		}

		return new AESEntity(input, pts, GlobalKey.getGlobalKey().getKey(), encode);
	}

	/**
	 * Returns the AESStrategy class.
	 * @return the AESStrategy class
	 */
	public Class<? extends AESStrategy> getStrategy() {
		return strategyClass;
	}

	/**
	 * Sets a new strategy.
	 * @param strategy a new AESStrategy
	 */
	public void setStrategy(Class<? extends AESStrategy> strategy) {
		assert strategy != null;

		strategyClass = strategy;
	}
}
