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

import ardat.tree.builder.ArchiveTreeBuilder;
import ardat.tree.builder.archive.Headers;
import io.SharedChannelFactory;
import io.SharedSeekableByteChannel;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.LinkedBlockingDeque;

public class ArchiveEntityFactory {

	private record HeaderLayer(String entityName, ArchiveEntityProperty[] pts) {}

	public static ArchiveEntity fromArchive(ArchiveTreeBuilder.ArchEntityInfo info, Path arch) throws IOException {
		assert info != null && arch != null;

		try (SeekableByteChannel sbc = Files.newByteChannel(arch, StandardOpenOption.READ)) {
			sbc.position(info.offset());
			ArchiveEntity entity = null;
			HeaderLayer[] layers = parseHeader(info.header());
			for (HeaderLayer layer : layers) {
				Path relativePath = Path.of(Headers.getRelativePath(info.header()));
				String entityName = relativePath.getName(relativePath.getNameCount() - 1).toString();
				if (layer.entityName().equals(DirectoryEntity.class.getSimpleName())) {
					entity = new DirectoryEntity(entityName, layer.pts());
				} else if (layer.entityName().equals(FileEntity.class.getSimpleName())) {
					long fileSize = Headers.getFileSize(info.header());
					int headerLength = info.header().length();
					SharedSeekableByteChannel content =
						SharedChannelFactory
							.getSharedChannelFactory()
							.newChannel(arch, info.offset() + headerLength, fileSize);
					entity = new FileEntity(entityName, content, layer.pts());
				}
			}

			return entity;
		}
	}

	private static HeaderLayer[] parseHeader(String header) {
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
	}
}
