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

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

/**
 * AESStrategy is an interface that provides a straightforward way for AES encryption and decryption. It allows
 * concrete implementations to choose the specific block cipher mode e.g. CTR, CBC. The barebone implementation
 * ( without extending the public interface ) implies that the padding and de-padding is left to the client.
 */
public sealed interface AESStrategy permits AESCBCStrategy, AESCTRStrategy {

	/**
	 * Encrypts the passed data.
	 * @param input the data
	 * @return the encrypted data
	 * @throws GeneralSecurityException if exceptions related to javax.crypto.Cipher occurred
	 */
	ByteBuffer encrypt(ByteBuffer input) throws GeneralSecurityException;

	/**
	 * Decrypts the encrypted data.
	 * @param input the encrypted data
	 * @return the decrypted data
	 * @throws GeneralSecurityException if exceptions related to javax.crypto.Cipher occurred
	 */
	ByteBuffer decrypt(ByteBuffer input) throws GeneralSecurityException;
}
