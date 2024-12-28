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

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.HexFormat;

/**
 * AESCTRStrategy is a concrete implementation of {@link AESStrategy} using the CTR block cipher mode.
 */
public final class AESCTRStrategy implements AESStrategy{

	private final int blockSize = 16;

	private final Key key;

	private final byte[] iv;

	private long counter = 0;

	/**
	 * Constructs AESCTRStrategy using the specified key and the nonce ( 64 bit initial vector ). The nonce is retrieved
	 * from pts where it is presented as an {@link ArchiveEntityProperty} with the "iv64" key and the hex formated value
	 * as the nonce.<br><br>
	 *
	 * Once constructed it can be used only for either encryption or decryption. To decrypt the encrypted blocks they
	 * need to be passed in the same order as the original data was during the encryption.
	 * @param symmetricKey the symmetric key
	 * @param pts archive properties containing the nonce
	 */
	public AESCTRStrategy(Key symmetricKey, ArchiveEntityProperty[] pts) {
		assert symmetricKey != null && pts != null;

		key = symmetricKey;
		try {
			iv = HexFormat.of().parseHex(ArchiveEntityProperty.findVal("iv64", pts));
			if (iv.length != 8)
				throw new IllegalArgumentException("Bad initial vector length: expected 8, parsed " + iv.length);
		} catch (IllegalArgumentException exception) {
			throw new ArchiveCorruptedException("Initial vector is corrupted: ", exception);
		}
	}

	@Override
	public ByteBuffer encrypt(ByteBuffer input) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		ByteBuffer output = ByteBuffer.allocate(input.remaining());
		byte[] block = new byte[blockSize];

		while (input.hasRemaining()) {
			input.get(block);
			output.put(xor(block, cipher.doFinal(computeCTR(iv, counter++))));
		}
		return output;
	}

	@Override
	public ByteBuffer decrypt(ByteBuffer input) throws GeneralSecurityException {
		return encrypt(input);
	}

	private byte[] computeCTR(byte[] iv, long counter) {
		byte[] ctr = new byte[blockSize];
		ctr[ 8] = (byte) ((counter >> 56) & 0xff);
		ctr[ 9] = (byte) ((counter >> 48) & 0xff);
		ctr[10] = (byte) ((counter >> 40) & 0xff);
		ctr[11] = (byte) ((counter >> 32) & 0xff);
		ctr[12] = (byte) ((counter >> 24) & 0xff);
		ctr[13] = (byte) ((counter >> 16) & 0xff);
		ctr[14] = (byte) ((counter >>  8) & 0xff);
		ctr[15] = (byte) ((counter >>  0) & 0xff);
		System.arraycopy(iv, 0, ctr, 0, iv.length);
		return ctr;
	}

	private byte[] xor(byte[] a, byte[] b) {
		byte[] c = new byte[blockSize];
		for (int i = 0; i < a.length; i++) {
			c[i] = (byte) (a[i] ^ b[i]);
		}
		return c;
	}
}
