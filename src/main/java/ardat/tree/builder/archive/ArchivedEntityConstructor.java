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

import ardat.tree.ArchiveEntityConstructorInterface;
import ardat.exceptions.ArchiveCorruptedException;
import ardat.tree.*;
import ardat.tree.builder.ArchiveTreeBuilder;
import io.GlobalKey;
import io.SharedChannelFactory;
import io.SharedSeekableByteChannel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * ArchivedEntityConstructor is an implementation of {@link ArchiveEntityConstructorInterface} that constructs
 * an {@link ArchiveEntity} object based on its archive header.
 */
public class ArchivedEntityConstructor implements ArchiveEntityConstructorInterface<ArchiveTreeBuilder.ArchEntityInfo> {

	private record HeaderLayer(String entityClass, ArchiveEntityProperty[] pts) {}

	private final Path arch;

	/**
	 * Constructs an ArchiveEntityConstructor using the provided archive path. The archive is read to retrieve
	 * the content of {@link FileEntity}.
	 * @param archive the archive path
	 */
	public ArchivedEntityConstructor(Path archive) {
		assert archive != null;

		arch = archive;
	}

	/**
	 * Returns an {@link ArchiveEntity} using the provided archive header and its offset in the archive.
	 * @param input the header-offset pair
	 * @return the constructed entity
	 */
	@Override
	public ArchiveEntity construct(ArchiveTreeBuilder.ArchEntityInfo input) {
		assert input != null;

		try {
			HeaderLayer[] layers = parseHeader(input.header());
			ArchiveEntity entity = constructArchiveEntity(layers, input);
			if (entity == null) {
				throw new ArchiveCorruptedException("File header corrupted: missing ArchiveEntity: " + input.header());
			}
			return entity;
		} catch (NullPointerException exception) {
			throw new ArchiveCorruptedException(
				"File header corrupted: missing/mis-ordered FileEntity: " + input.header(),
				exception
			);
		} catch (IllegalArgumentException exception) {
			throw new ArchiveCorruptedException(
				"File header corrupted: the path is unresolved: " + input.header(),
				exception
			);
		} catch (IOException exception) {
			throw new RuntimeException("Failed to created SharedSeekableByteChannel: ", exception);
		}
	}

	private HeaderLayer[] parseHeader(String header) {
		try {
			header = header.trim();
			int charsLeft = header.length();
			LinkedBlockingDeque<HeaderLayer> layers = new LinkedBlockingDeque<>();
			LinkedBlockingDeque<ArchiveEntityProperty> pts = new LinkedBlockingDeque<>();
			while (charsLeft > 0) {
				int nextLineSepIndex = header.lastIndexOf('\n', charsLeft - 1);
				String headerLine = header.substring(nextLineSepIndex + 1, charsLeft);
				charsLeft -= headerLine.length() + 1;
				if (headerLine.startsWith("class ")) {
					String className = headerLine.substring(headerLine.indexOf(' ') + 1);
					layers.push(new HeaderLayer(className, pts.toArray(new ArchiveEntityProperty[0])));
					pts = new LinkedBlockingDeque<>();
				} else {
					String key = headerLine.substring(0, headerLine.indexOf(' '));
					String val = headerLine.substring(headerLine.indexOf(' ') + 1);
					pts.push(new ArchiveEntityProperty(key, val));
				}
			}
			return layers.toArray(new HeaderLayer[0]);
		} catch (IndexOutOfBoundsException exception) {
			throw new ArchiveCorruptedException(
				"File header corrupted: header structure is corrupted: " + header,
				exception
			);
		}
	}

	// removes the last metaheader none and flips the decorators if any present
	private HeaderLayer[] headerArranger(HeaderLayer[] layers) {
		HeaderLayer[] arrangedLayers = new HeaderLayer[layers.length - 1];
		arrangedLayers[0] = layers[0];
		for (int i = 1; i < arrangedLayers.length; i++) {
			arrangedLayers[i] = layers[layers.length - 1 - i];
		}

		return arrangedLayers;
	}

	private ArchiveEntity constructArchiveEntity(
		HeaderLayer[] layers,
		ArchiveTreeBuilder.ArchEntityInfo info
	) throws IOException {
		ArchiveEntity entity = null;
		Path entityPath = Path.of(Headers.getRelativePath(info.header()));
		String entityName = entityPath.getName(entityPath.getNameCount() - 1).toString();
		for (HeaderLayer layer: headerArranger(layers)) {
			String entityClass = layer.entityClass();
			if (entityClass.equals(DirectoryEntity.class.getSimpleName())) {
				entity = new DirectoryEntity(entityName, layer.pts());
			} else if (entityClass.equals(FileEntity.class.getSimpleName())) {
				long fileSize = Headers.getFileSize(info.header());
				int headerLength = info.header().length();
				SharedSeekableByteChannel content =
					SharedChannelFactory
						.getSharedChannelFactory()
						.newChannel(arch, info.offset() + headerLength, fileSize);
				entity = new FileEntity(entityName, content, layer.pts());
			} else if (entityClass.equals(AESEntity.class.getSimpleName())) {
				entity = new AESEntity(entity, layer.pts(), GlobalKey.getGlobalKey().getKey(), false);
			} else if (entityClass.equals(PrettyEntity.class.getSimpleName())) {
				entity = new PrettyEntity(entity, false);
			}
		}

		return entity;
	}
}
