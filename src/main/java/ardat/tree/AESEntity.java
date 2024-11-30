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
import crypto.AESStrategy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * AESEntity is a concrete implementation of {@link ArchiveEntityProcessor}, that provides AES encryption and decryption
 * of the data of the underlying entity. The specific algorithm of encryption and decryption is specified by
 * {@link AESStrategy}.
 */
public class AESEntity extends ArchiveEntityProcessor {

	private final int aesBlockSize = 16;

	private final int silentHeaderLength = aesBlockSize * 2;

	private final ByteBuffer potentialHeader = ByteBuffer.allocate(silentHeaderLength);

	private AESStrategy strat;

	private MessageDigest digest;

	private final ArchiveEntityProperty[] aesProperties;

	/**
	 * Constructs AESEntity using the provided archive entity and encryption algorithm.
	 * @param archiveEntity an archive entity
	 * @param pts the properties related to AESEntity
	 * @param strategy the encryption algorithm
	 * @param encode an operation mode
	 */
	public AESEntity(ArchiveEntity archiveEntity, ArchiveEntityProperty[] pts, AESStrategy strategy, boolean encode) {
		assert archiveEntity != null && pts != null && strategy != null;

		aesProperties = pts;
		potentialHeader.limit(0);
		setComponent(archiveEntity);
		setEncode(encode);
		setStrategy(strategy);
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ignored) {}
	}

	/**
	 * Constructs AESEntity using the provided archive entity and encryption algorithm.
	 * @param archiveEntity an archive entity
	 * @param pts the properties related to AESEntity
	 * @param strategy the encryption algorithm
	 */
	public AESEntity(ArchiveEntity archiveEntity, ArchiveEntityProperty[] pts, AESStrategy strategy) {
		this(archiveEntity, pts, strategy, true);
	}

	/**
	 * Sets a new encryption algorithm.
	 * @param strategy a new encryption algorithm
	 */
	public void setStrategy(AESStrategy strategy) {
		assert strategy != null;

		strat = strategy;
	}

	/**
	 * Returns the current encryption algorithm.
	 * @return the current encryption algorithm
	 */
	public AESStrategy getStrategy() {
		return strat;
	}

	@Override
	protected int encode(ByteBuffer in, ByteBuffer out) throws IOException {
		out.put(strat.encrypt(in));
		digest.update(in.array(), 0, in.limit());
		if (!getComponent().hasRemainingContent()) {
			out.put(digest.digest());
		}
		return out.position();
	}

	@Override
	protected int decode(ByteBuffer in, ByteBuffer out) throws IOException {
		ByteBuffer encodedData = ByteBuffer.allocate(potentialHeader.remaining() + in.remaining());
		encodedData.put(potentialHeader).put(in);
		encodedData.flip();

		potentialHeader.clear();
		potentialHeader.put(0, encodedData, encodedData.capacity() - silentHeaderLength, silentHeaderLength);
		ByteBuffer encryptedData = encodedData.limit(encodedData.limit() - silentHeaderLength);
		out.put(strat.decrypt(encryptedData));
		digest.update(out.array(), 0, out.position());

		if (!getComponent().hasRemainingContent()) {
			if (!Arrays.equals(potentialHeader.array(), digest.digest())) {
				throw new ArchiveCorruptedException(
					"The content of the " + Path.of("", getName()) + " file is corrupt"
				);
			}
		}

		return out.position();
	}

	@Override
	protected int getPreferredUnprocessedWindowSize() {
		return aesBlockSize + silentHeaderLength;
	}

	@Override
	protected int getPreferredProcessedWindowSize() {
		return getPreferredUnprocessedWindowSize() + silentHeaderLength;
	}

	/**
	 * Returns properties of the underlying entity with the current properties attached to the end.
	 * @return properties of the underlying entity with the current properties attached to the end
	 */
	@Override
	public ArchiveEntityProperty[] getProperties() {
		ArchiveEntityProperty[] parentPts = super.getProperties();
		ArchiveEntityProperty[] allPts = Arrays.copyOf(parentPts, parentPts.length + aesProperties.length + 1);
		allPts[parentPts.length] = new ArchiveEntityProperty("class", getClass().getSimpleName());
		System.arraycopy(aesProperties, 0, allPts, parentPts.length + 1, aesProperties.length);
		return allPts;
	}
}
