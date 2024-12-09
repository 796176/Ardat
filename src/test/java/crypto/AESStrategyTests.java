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

import ardat.tree.ArchiveEntityProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AESStrategyTests {

	static final Key key = new SecretKeySpec(new byte[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, "AES");

	static final ArchiveEntityProperty[] pts = new ArchiveEntityProperty[] {
		new ArchiveEntityProperty("iv64", "0001020304050607"),
		new ArchiveEntityProperty("iv128", "000102030405060708090a0b0c0d0e0f")
	};

	ByteBuffer oneBlock() {
		byte[] block = new byte[16];
		for (int i = 0; i < block.length; i++) {
			block[i] = (byte) i;
		}
		return ByteBuffer.wrap(block);
	}

	ByteBuffer twoBlocks() {
		byte[] blocks = new byte[32];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = (byte) i;
		}
		return ByteBuffer.wrap(blocks);
	}

	ByteBuffer tenBlocks() {
		byte[] blocks = new byte[160];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = (byte) i;
		}
		return ByteBuffer.wrap(blocks);
	}

	@ParameterizedTest
	@ValueSource(classes = {AESCTRStrategy.class, AESCTRStrategy.class})
	void encryptionDecryptionTests(Class<AESStrategy> cl) throws ReflectiveOperationException {
		AESStrategy encryption = cl.getConstructor(Key.class, ArchiveEntityProperty[].class).newInstance(key, pts);
		AESStrategy decryption = cl.getConstructor(Key.class, ArchiveEntityProperty[].class).newInstance(key, pts);
		assertDoesNotThrow(() -> {
			assertEquals(
				decryption.decrypt(encryption.encrypt(oneBlock()).flip()).flip(),
				oneBlock(),
				"One block encryption-with-decryption integrity failed"
			);
			assertEquals(
				decryption.decrypt(encryption.encrypt(twoBlocks()).flip()).flip(),
				twoBlocks(),
				"Two blocks encryption-with-decryption integrity failed"
			);
			assertEquals(
				decryption.decrypt(encryption.encrypt(tenBlocks()).flip()).flip(),
				tenBlocks(),
				"Ten blocks encryption-with-decryption integrity failed"
			);
		});
	}
}
