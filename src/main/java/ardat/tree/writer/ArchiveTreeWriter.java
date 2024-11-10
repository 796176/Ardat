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

package ardat.tree.writer;

import ardat.tree.ArchiveEntity;
import ardat.tree.ArchiveEntityProperty;
import ardat.format.Metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ArchiveTreeWriter extends AbstractTreeWriter {

	private final Path archPath;

	private ArchiveTreeWriter(Path archive) throws IOException {
		assert archive != null;

		archPath = archive;
		writeArchiveMetadata();
	}

	public static ArchiveTreeWriter getArchiveTreeWriter(Path archive) throws IOException {
		assert archive != null;
		if (Files.exists(archive)) throw new IOException("The file " + archive + " already exists");

		return new ArchiveTreeWriter(archive);
	}

	@Override
	protected void writeArchiveEntity(ArchiveEntity entity) throws IOException {
		try (SeekableByteChannel sbc = Files.newByteChannel(archPath, StandardOpenOption.WRITE)) {
			sbc.position(sbc.size());
			ArchiveEntityProperty[] pts = entity.getProperties();
			for (ArchiveEntityProperty property: pts) {
				String ptLine = property.key() + " " + property.val() + "\n";
				sbc.write(ByteBuffer.wrap(ptLine.getBytes()));
			}

			sbc.write(ByteBuffer.wrap("class none\n".getBytes()));
			String relativeName = String.join("/", entity.getName());
			sbc.write(ByteBuffer.wrap(("filepath %s\n".formatted(relativeName)).getBytes()));
			sbc.write(ByteBuffer.wrap(("children %d\n".formatted(entity.getChildren().length)).getBytes()));

			long sizePos = sbc.position();
			sbc.write(ByteBuffer.wrap("size %016x\n".formatted(10).getBytes()));
			sbc.write(ByteBuffer.wrap(new byte[] {'\n'}));

			long size = 0;
			ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
			while (entity.getContent(buffer) > 0) {
				buffer.flip();
				size += sbc.write(buffer);
				buffer.clear();
			}
			sbc.position(sizePos);
			sbc.write(ByteBuffer.wrap("size %016x".formatted(size).getBytes()));
		}
	}

	private void writeArchiveMetadata() throws IOException {
		Metadata.MetadataBuilder builder = Metadata.getBuilder();
		builder.addProperty("version", "0.1");
		builder.addProperty("origins", System.getProperty("os.name"));
		String meta = builder.build().toString() + "\n";
		try(ByteChannel bc = Files.newByteChannel(archPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			ByteBuffer buffer = ByteBuffer.allocate(meta.length());
			buffer.put(meta.getBytes());
			buffer.flip();
			bc.write(buffer);
		}
	}
}
