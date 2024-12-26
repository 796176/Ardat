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

package ardat;

import ardat.tree.AESEntityConfigurator;
import ardat.tree.FileEntityDecorator;
import ardat.tree.builder.ArchiveTreeBuilder;
import ardat.tree.builder.FSTreeBuilder;
import ardat.tree.builder.TreeBuilder;
import ardat.tree.writer.AbstractTreeWriter;
import ardat.tree.writer.ArchiveTreeWriter;
import ardat.tree.writer.FSTreeWriter;
import crypto.AESCBCStrategy;
import io.GlobalKey;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Key;

/**
 * ArdatFacade contains static methods to provide general use of Ardat.
 */
public class ArdatFacade {
	/**
	 * Archives the given file or directory.
	 * @param from the path to a file or directory to archive
	 * @param to the path to the archive file
	 */
	public static void archive(Path from, Path to) {
		try {
			TreeBuilder builder = new FSTreeBuilder(from);
			builder.build();
			AbstractTreeWriter writer = ArchiveTreeWriter.getArchiveTreeWriter(to);
			writer.write();
		} catch (IOException exception) {
			throw new RuntimeException("Unexpected exception occurred: ", exception);
		}
	}

	/**
	 * Extracts the given archive file to a specified directory.
	 * @param from the path to the archive file
	 * @param to the directory to write the archived content to
	 */
	public static void extract(Path from, Path to) {
		try {
			TreeBuilder builder = ArchiveTreeBuilder.getArchiveTreeBuilder(from);
			builder.build();
			AbstractTreeWriter writer = FSTreeWriter.getFSTreeWriter(to);
			writer.write();
		} catch (IOException exception) {
			throw new RuntimeException("Unexpected exception occurred: ", exception);
		}
	}

	/**
	 * Archives the given file or directory and encrypts the content of every file
	 * @param from the path to a file or directory to archive
	 * @param to the path to the archive file
	 * @param key the AES compatible key
	 */
	public static void archiveEncrypted(Path from, Path to, Key key) {
		try {
			GlobalKey.getGlobalKey().setKey(key);
			TreeBuilder builder = new FSTreeBuilder(from);
			FileEntityDecorator decorator = new FileEntityDecorator();
			decorator.getDecoratorList().add(new AESEntityConfigurator(AESCBCStrategy.class));
			builder.setDecorator(decorator);
			builder.build();
			AbstractTreeWriter writer = ArchiveTreeWriter.getArchiveTreeWriter(to);
			writer.write();
		} catch (IOException exception) {
			throw new RuntimeException("Unexpected exception occurred: ", exception);
		}
	}

	/**
	 * Extracts the given archive file with encrypted content to a specified directory.
	 * @param from the path to the archive file
	 * @param to the directory to write the archived content to
	 * @param key the same key that was used to encrypt the content
	 */
	public static void extractEncrypted(Path from, Path to, Key key) {
		GlobalKey.getGlobalKey().setKey(key);
		extract(from, to);
	}
}
