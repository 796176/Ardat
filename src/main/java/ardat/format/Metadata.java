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

import java.util.LinkedList;

public class Metadata {

	private record KeyValueTuple(String key, String val) {}

	private final String signature = "ardat";

	private final LinkedList<KeyValueTuple> properties = new LinkedList<>();

	private static Metadata instance = null;

	private Metadata() {}

	public String getSignature() {
		return signature;
	}

	public String getProperty(String key) {
		assert key != null;

		for (KeyValueTuple t: properties) {
			if (t.key.equals(key)) return t.val;
		}
		return null;
	}

	public static Metadata getMetadata() {
		return instance;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("""
			%s
			%d""".formatted(getSignature(), properties.size() + 2));
		for (KeyValueTuple t: properties) {
			builder.append("\n%s %s".formatted(t.key, t.val));
		}
		return builder.toString();
	}

	public static MetadataBuilder getBuilder() {
		return new MetadataBuilder();
	}

	public static class MetadataBuilder {

		private final Metadata metadata = new Metadata();

		private int linesFed = 0;

		private int metaHeaderLineAmount;

		private MetadataBuilder() {}

		public boolean feedPropertyLine(String line) {
			assert line != null;

			return processLine(line.strip(), linesFed++);
		}

		public void addProperty(String key, String val) {
			assert key != null && val != null;

			metadata.properties.add(new KeyValueTuple(key, val));
		}

		public Metadata build() {
			instance = metadata;
			return metadata;
		}

		private boolean processLine(String line, int lineCount) {
			if (lineCount == 0) {
				if (!line.equals(metadata.getSignature())) {
					throw new ArchiveCorruptedException("The file signature doesn't match");
				}
				return true;
			}
			if (lineCount == 1) {
				try {
					metaHeaderLineAmount = Integer.parseInt(line);
					return true;
				} catch (NumberFormatException exception) {
					throw new ArchiveCorruptedException(
						"Metadata corrupted: expected the length of the metadata header"
					);
				}
			}

			try {
				String key = line.substring(0, line.indexOf(' '));
				String val = line.substring(line.indexOf(' ') + 1);
				addProperty(key, val);
				return  (!(lineCount + 1 == metaHeaderLineAmount));
			} catch (IndexOutOfBoundsException exception) {
				throw new ArchiveCorruptedException(
					"Metadata corrupted: expected a key-value pair at line " + (lineCount + 1)
				);
			}
		}
	}
}
