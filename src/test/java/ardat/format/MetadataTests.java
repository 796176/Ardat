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

package ardat.format;

import ardat.exceptions.ArchiveCorruptedException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataTests {
	@Test
	void failSignature() {
		Metadata.MetadataBuilder builder = Metadata.getBuilder();
		assertThrows(ArchiveCorruptedException.class, () -> builder.feedPropertyLine("ligma"));
	}

	@Test
	void failLength() {
		Metadata.MetadataBuilder builder = Metadata.getBuilder();
		assertThrows(ArchiveCorruptedException.class, () -> {
			builder.feedPropertyLine("ardat");
			builder.feedPropertyLine("four");
		});
	}

	@Test
	void failProperty() {
		Metadata.MetadataBuilder builder = Metadata.getBuilder();
		assertThrows(ArchiveCorruptedException.class, () -> {
			builder.feedPropertyLine("ardat");
			builder.feedPropertyLine("3");
			builder.feedPropertyLine("none");
		});
	}

	@Nested
	class Building {
		static String metadataHeader = """
			ardat
			5
			key1 val1
			key2 val2
			key3 val3
			""";

		@Test
		void buildTest() {
			Metadata.MetadataBuilder builder = Metadata.getBuilder();
			String[] lines = metadataHeader.lines().toArray(String[]::new);
			for (int i = 0; i < lines.length - 1; i++) {
				assertTrue(builder.feedPropertyLine(lines[i]));
			}
			assertFalse(builder.feedPropertyLine(lines[lines.length - 1]));
			builder.build();
		}

		@Nested
		class Built {
			@Test
			void checkProperties() {
				Metadata metadata = Metadata.getMetadata();
				assertEquals(metadata.getProperty("key1"), "val1");
				assertEquals(metadata.getProperty("key2"), "val2");
				assertEquals(metadata.getProperty("key3"), "val3");
			}
		}
	}
}