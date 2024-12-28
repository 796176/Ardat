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


import ardat.tree.builder.ArraySeekableByteChannel;
import crypto.AESCBCStrategy;
import crypto.AESCTRStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Iterator;
import java.util.LinkedList;

public class AESEntityTests {
	@ParameterizedTest
	@MethodSource("entityProvider")
	void encryptTest(AESEntity aesEntity, ByteBuffer encryptedBuffer) throws IOException {
		ArchiveEntityProcessorTests tests = new ArchiveEntityProcessorTests() {
			@Override
			ArchiveEntityProcessor getInstance(ArchiveEntity archiveEntity) {
				aesEntity.setComponent(archiveEntity);
				return aesEntity;
			}
			@Override
			ByteBuffer getEncodedData() {
				return encryptedBuffer;
			}
			@Override
			ByteBuffer getDecodedData() {
				return ByteBuffer.wrap("We won't forget".getBytes());
			}
		};
		tests.encodeTest();
	}

	@ParameterizedTest
	@MethodSource("entityProvider")
	void decryptTest(AESEntity aesEntity, ByteBuffer encryptedBuffer) throws IOException {
		ArchiveEntityProcessorTests tests = new ArchiveEntityProcessorTests() {
			@Override
			ArchiveEntityProcessor getInstance(ArchiveEntity archiveEntity) {
				aesEntity.setComponent(archiveEntity);
				return aesEntity;
			}
			@Override
			ByteBuffer getEncodedData() {
				return encryptedBuffer;
			}
			@Override
			ByteBuffer getDecodedData() {
				return ByteBuffer.wrap("We won't forget".getBytes());
			}
		};
		tests.encodeTest();
	}

	static Iterator<Arguments> entityProvider() {
		LinkedList<Arguments> ll = new LinkedList<>();
		Key key = new SecretKeySpec(new byte[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, "AES");
		ArchiveEntity entity =
			new FileEntity("", new ArraySeekableByteChannel(new byte[0]), new ArchiveEntityProperty[]{});

		ArchiveEntityProperty[] cbcPts =
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("strategy", AESCBCStrategy.class.getName()),
				new ArchiveEntityProperty("iv128", "000102030405060708090a0b0c0d0e0f")
			};
		ll.add(Arguments.of(
			new AESEntity(entity, cbcPts, key),
			ByteBuffer.wrap(new byte[]{
				111, 65, -92, -7, 12, 81, 99, -80, -81, 35, -121, 53, -78, -119, 12, -112, -112, -26, 68, 79, -100, 52,
				99, 30, 8, -20, 3, 97, -10, 37, -52, -75, 21, 29, -34, 101, -62, 114, -27, 98, 74, 11, -80, -41, -15,
				-29, -105, -7
			})
		));

		ArchiveEntityProperty[] ctrPts =
			new ArchiveEntityProperty[] {
				new ArchiveEntityProperty("strategy", AESCTRStrategy.class.getName()),
				new ArchiveEntityProperty("iv64", "0001020304050607")
			};
		ll.add(Arguments.of(
			new AESEntity(entity, ctrPts, key),
			ByteBuffer.wrap(new byte[]{
				-14, 93, -61, 27, 104, -83, -103, -11, 0, -96, 126, -122, 57, 72, 4, -124, -112, -26, 68, 79, -100, 52,
				99, 30, 8, -20, 3, 97, -10, 37, -52, -75, 21, 29, -34, 101, -62, 114, -27, 98, 74, 11, -80, -41, -15,
				-29, -105, -7
			})
		));

		return ll.iterator();
	}
}
