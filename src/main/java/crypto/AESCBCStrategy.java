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

import ardat.exceptions.ArchiveCorruptedException;
import ardat.tree.ArchiveEntityProperty;

import javax.crypto.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * AESCBCStrategy is a concrete implementation of {@link AESStrategy} using the CBC cipher block mode.
 */
public final class AESCBCStrategy implements AESStrategy {

	private final int blockSize = 16;

	private final Key key;

	private byte[] iv;

	/**
	 * Constructs AESCBCStrategy with the specified key and the initial vector. The initial vector is retrieved from pts
	 * where it is presented as {@link ArchiveEntityProperty} with the "iv128" key and the hex formated value as the
	 * initial vector.<br><br>
	 * Once constructed it can be used only for either encryption or decryption. To decrypt the encrypted blocks they
	 * need to be passed in the same order as the original data was during the encryption.
	 * @param symmetricKey the symmetric key
	 * @param pts archive properties containing the initial vector
	 */
	public AESCBCStrategy(Key symmetricKey, ArchiveEntityProperty[] pts) {
		assert symmetricKey != null && pts != null;

		key = symmetricKey;
		try {
			iv = HexFormat.of().parseHex(ArchiveEntityProperty.findVal("iv128", pts));
			if (iv.length != 16)
				throw new IllegalArgumentException("Bad initial vector length: expected 16, parsed " + iv.length);
		} catch (IllegalArgumentException e) {
			throw new ArchiveCorruptedException("Initial vector corrupted: ", e);
		}
	}

	/**
	 * Encrypts the padded data.
	 * @param input the padded data
	 * @return the encrypted data
	 */
	@Override
	public ByteBuffer encrypt(ByteBuffer input) {
		assert input.remaining() % blockSize == 0;

		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			ByteBuffer outputBuffer = ByteBuffer.allocate(input.remaining());

			byte[] block = new byte[blockSize];
			while (input.hasRemaining()) {
				input.get(block);
				cipher.doFinal(xor(block, iv), 0, blockSize, iv, 0);
				outputBuffer.put(iv);
			}
			return outputBuffer;
		} catch (
			NoSuchAlgorithmException |
			NoSuchPaddingException |
			BadPaddingException |
			ShortBufferException |
			IllegalBlockSizeException |
			InvalidKeyException e
		) {
			throw new RuntimeException("This exception shouldn't have been thrown, severe bug detected");
		}
	}

	/**
	 * Decrypt the encrypted data.
	 * @param input the encrypted data
	 * @return the decrypted data
	 */
	@Override
	public ByteBuffer decrypt(ByteBuffer input) {
		assert input.remaining() % blockSize == 0;

		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			ByteBuffer outputBuffer = ByteBuffer.allocate(input.remaining());

			byte[] block = new byte[blockSize];
			while (input.hasRemaining()) {
				input.get(block);
				outputBuffer.put(xor(cipher.doFinal(block), iv));
				iv = Arrays.copyOf(block, block.length);
			}
			return outputBuffer;
		} catch (
			NoSuchAlgorithmException |
			NoSuchPaddingException |
			BadPaddingException |
			IllegalBlockSizeException |
			InvalidKeyException ignored
		) {
			throw new RuntimeException("This exception shouldn't have been thrown, severe bug detected");
		}
	}

	private byte[] xor(byte[] a, byte[] b) {
		byte[] c = new byte[blockSize];
		for (int i = 0; i < a.length; i++) {
			c[i] = (byte) (a[i] ^ b[i]);
		}
		return c;
	}
}
