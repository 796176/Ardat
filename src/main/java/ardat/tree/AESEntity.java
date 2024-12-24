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
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * AESEntity is a concrete implementation of {@link ArchiveEntityProcessor}, that provides AES encryption and decryption
 * of the data of the underlying entity. The specific algorithm of encryption and decryption is specified by
 * {@link AESStrategy}.
 */
public class AESEntity extends ArchiveEntityProcessor {

	private final int aesBlockSize = 16;

	private final int silentHeaderLength = aesBlockSize * 2;

	private final int maxPaddingLength = aesBlockSize;

	private final ByteBuffer potentialHeader = ByteBuffer.allocate(silentHeaderLength);

	private AESStrategy strat;

	private MessageDigest digest;

	private final ArchiveEntityProperty[] aesProperties;

	/**
	 * Constructs AESEntity using the provided archive entity and encryption algorithm. The encryption algorithm is
	 * specified by {@link AESStrategy} and passed through the parameters alongside the parameters required by
	 * a concrete implementation of the strategy.
	 * @param archiveEntity an archive entity
	 * @param pts the properties related to AESEntity
	 * @param key the symmetric key
	 * @param encode an operation mode
	 * @throws NullPointerException if archiveEntity is null
	 */
	public AESEntity(ArchiveEntity archiveEntity, ArchiveEntityProperty[] pts, Key key, boolean encode) {
		assert pts != null && key != null;
		if (archiveEntity == null) throw new NullPointerException();

		aesProperties = pts;
		potentialHeader.limit(0);
		setComponent(archiveEntity);
		setEncode(encode);
		try {
			digest = MessageDigest.getInstance("SHA-256");
			strat = (AESStrategy) Class
				.forName(ArchiveEntityProperty.findVal("strategy", pts))
				.getConstructor(Key.class, ArchiveEntityProperty[].class)
				.newInstance(key, pts);
		} catch (Exception exception) {
			throw new ArchiveCorruptedException("The AESStrategy construction failed", exception);
		}
	}

	/**
	 * Constructs AESEntity using the provided archive entity and encryption algorithm. The encryption algorithm is
	 * specified by {@link AESStrategy} and passed through the parameters alongside the parameters required by
	 * a concrete implementation of the strategy.
	 * @param archiveEntity an archive entity
	 * @param key the symmetric key
	 * @param pts the properties related to AESEntity
	 * @throws NullPointerException if archiveEntity is null
	 */
	public AESEntity(ArchiveEntity archiveEntity, ArchiveEntityProperty[] pts, Key key) {
		this(archiveEntity, pts, key, true);
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
		try {
			if (getComponent().hasRemainingContent()) {
				out.put(strat.encrypt(in).flip());
				digest.update(in.array(), 0, in.limit());
			} else {
				int padLength = aesBlockSize - in.remaining() % aesBlockSize;
				byte[] padding = new byte[padLength];
				Arrays.fill(padding, (byte) padLength);
				ByteBuffer paddedBuffer = ByteBuffer.allocate(in.remaining() + padLength);
				paddedBuffer.put(in).put(padding).position(0);

				out.put(strat.encrypt(paddedBuffer).flip());
				digest.update(paddedBuffer.array());
				out.put(digest.digest());
			}
			return out.position();
		} catch (GeneralSecurityException exception) {
			throw new ArchiveCorruptedException("Unexpected exception occurred: ", exception);
		}
	}

	@Override
	protected int decode(ByteBuffer in, ByteBuffer out) throws IOException {
		try {
			ByteBuffer encodedData = ByteBuffer.allocate(potentialHeader.remaining() + in.remaining());
			encodedData.put(potentialHeader).put(in);
			encodedData.flip();
			potentialHeader.clear();
			potentialHeader
				.put(0, encodedData, encodedData.capacity() - silentHeaderLength, silentHeaderLength);

			ByteBuffer encryptedData = encodedData.limit(encodedData.limit() - silentHeaderLength);
			out.put(strat.decrypt(encryptedData).flip());
			digest.update(out.array(), 0, out.position());
		} catch (IndexOutOfBoundsException | GeneralSecurityException exception) {
			throw new ArchiveCorruptedException(
				"The content of the " + Path.of("", getName()) + " file corrupted: ",
				exception
			);
		}

		if (!getComponent().hasRemainingContent()) {
			if (!Arrays.equals(potentialHeader.array(), digest.digest())) {
				throw new ArchiveCorruptedException(
					"The content of the " + Path.of("", getName()) + " file corrupted: hash mismatch"
				);
			}
			int padLength = out.get(out.position() - 1);
			out.position(out.position() - padLength);
		}

		return out.position();
	}

	@Override
	protected int getPreferredUnprocessedWindowSize() {
		return aesBlockSize + silentHeaderLength;
	}

	@Override
	protected int getPreferredProcessedWindowSize() {
		return getPreferredUnprocessedWindowSize() + maxPaddingLength + silentHeaderLength;
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
