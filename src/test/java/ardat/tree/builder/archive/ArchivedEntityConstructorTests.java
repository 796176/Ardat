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

package ardat.tree.builder.archive;

import ardat.tree.*;
import ardat.tree.builder.ArchiveTreeBuilder;
import ardat.tree.builder.ArraySeekableByteChannel;
import io.GlobalKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ArchivedEntityConstructorTests {
	@ParameterizedTest
	@MethodSource("parameterProvider")
	void constructionTests(
		ArchiveTreeBuilder.ArchEntityInfo info,
		ArchiveEntity expectedEntity,
		Path arch
	) throws IOException {
		ArchivedEntityConstructor constructor = new ArchivedEntityConstructor(arch);
		ArchiveEntity entity = constructor.construct(info);

		assertInstanceOf(
			expectedEntity.getClass(),
			entity,
			"The constructed entity is not " + expectedEntity.getClass().getSimpleName());
		assertArrayEquals(expectedEntity.getName(), entity.getName(), "The names don't match");
		assertArrayEquals(
			expectedEntity.getProperties(),
			entity.getProperties(),
			"The properties don't match"
		);

		ByteBuffer actualContent = ByteBuffer.allocate(1024);
		entity.getContent(actualContent);
		ByteBuffer expectedContent = ByteBuffer.allocate(1024);
		expectedEntity.getContent(expectedContent);
		assertEquals(expectedContent.flip(), actualContent.flip(), "The contents don't match");

	}

	static Stream<Arguments> parameterProvider() throws IOException {
		return Stream.of(getFileArguments(), getDirectoryArguments(), getComposedFileArguments());
	}

	static Arguments getFileArguments() throws IOException{
		Path archivePath = Path.of(
			System.getProperty("user.dir"),
			"src/test/resources/test_archives",
			"file1_" + System.getProperty("os.name").toLowerCase() + ".ardat"
		);
		String archiveContent = new String(Files.readAllBytes(archivePath));
		int headerOffset = archiveContent.indexOf("class FileEntity");
		String header = archiveContent.substring(headerOffset, archiveContent.indexOf("\n\n") + 2);
		ArchiveTreeBuilder.ArchEntityInfo entityInfo = new ArchiveTreeBuilder.ArchEntityInfo(headerOffset, header);
		FileEntity fileEntity = new FileEntity(
			"file1",
			new ArraySeekableByteChannel("qwerty".getBytes()),
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "1"),
				new ArchiveEntityProperty("modify-time", "1"),
				new ArchiveEntityProperty("access-time", "1")
			}
		);
		return Arguments.of(entityInfo, fileEntity, archivePath);
	}

	static Arguments getDirectoryArguments() throws IOException {
		Path archivePath = Path.of(
			System.getProperty("user.dir"),
			"src/test/resources/test_archives",
			"dir1_" + System.getProperty("os.name").toLowerCase() + ".ardat"
		);
		String archiveContent = new String(Files.readAllBytes(archivePath));
		int headerOffset = archiveContent.indexOf("class DirectoryEntity");
		String header = archiveContent.substring(headerOffset);
		ArchiveTreeBuilder.ArchEntityInfo info = new ArchiveTreeBuilder.ArchEntityInfo(headerOffset, header);
		DirectoryEntity directoryEntity = new DirectoryEntity(
			"dir1",
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "1"),
				new ArchiveEntityProperty("modify-time", "1"),
				new ArchiveEntityProperty("access-time", "1")
			}
		);
		return Arguments.of(info, directoryEntity, archivePath);
	}

	static Arguments getComposedFileArguments() throws IOException {
		Path archivePath = Path.of(
			System.getProperty("user.dir"),
			"src/test/resources/test_archives",
			"composed_file_" + System.getProperty("os.name").toLowerCase() + ".ardat"
		);
		String archiveContent = new String(Files.readAllBytes(archivePath));
		int headerOffset = archiveContent.indexOf("class FileEntity");
		String header = archiveContent.substring(headerOffset, archiveContent.indexOf("\n\n") + 2);
		ArchiveTreeBuilder.ArchEntityInfo info = new ArchiveTreeBuilder.ArchEntityInfo(headerOffset, header);

		FileEntity fileEntity = new FileEntity(
			"composed_file",
			new ArraySeekableByteChannel(
				"6dbb359b238c71781b53cbef28fddd70504c3608f8a479f9ef8f94160b0536f6a4c99359c314da5c116eec087c4f9e82"
					.getBytes()
			),
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("create-time", "1"),
				new ArchiveEntityProperty("modify-time", "1"),
				new ArchiveEntityProperty("access-time", "1")
			}
		);
		PrettyEntity prettyEntity = new PrettyEntity(fileEntity, false);
		Key aesKey = new SecretKeySpec(new byte[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, "AES");
		AESEntity aesEntity = new AESEntity(
			prettyEntity,
			new ArchiveEntityProperty[]{
				new ArchiveEntityProperty("strategy", "crypto.AESCBCStrategy"),
				new ArchiveEntityProperty("iv128", "aeba7524752a5b6393247b144a73c4cf"),
			},
			aesKey,
			false
		);

		GlobalKey.getGlobalKey().setKey(aesKey);

		return Arguments.of(info, aesEntity, archivePath);
	}
}
