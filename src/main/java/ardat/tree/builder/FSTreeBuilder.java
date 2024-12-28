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

package ardat.tree.builder;

import ardat.tree.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

/**
 * FSTreeBuilder is a concrete implementation of {@link TreeBuilder}, that constructs a new entity tree out of stored
 * files and directories. Every {@link ArchiveEntity} instantiated by this class is passed through
 * getDecorator().construct() to add additional functionality to the entity.
 */
public class FSTreeBuilder extends TreeBuilder{

	private final Path rootPath;

	/**
	 * Constructs a new instance of FSTreeBuilder.
	 * @param root the path to the file or directory
	 */
	public FSTreeBuilder(Path root) {
		assert root != null;

		rootPath = root;
	}
	@Override
	protected ArchiveEntity getRoot() throws IOException {
		return getDecorator().construct(buildArchiveEntity(rootPath));
	}

	@Override
	protected ArchiveEntity[] getChildren(ArchiveEntity entity) throws IOException {
		assert entity != null;

		Path concretePath = Path.of(rootPath.getParent().toString(), entity.getName());
		if (!Files.isDirectory(concretePath)) return new ArchiveEntity[0];

		LinkedList<ArchiveEntity> children = new LinkedList<>();
		Files.walkFileTree(concretePath, Set.of(), 1, new SimpleFileVisitor<>(){
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				children.add(getDecorator().construct(buildArchiveEntity(file)));
				return FileVisitResult.CONTINUE;
			}
		});
		return children.toArray(ArchiveEntity[]::new);
	}

	private ArchiveEntity buildArchiveEntity(Path path) throws IOException {
		String localName = path.getName(path.getNameCount() - 1).toString();

		BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
		ArrayList<ArchiveEntityProperty> properties = new ArrayList<>();

		long create = attributes.creationTime().toMillis();
		properties.add(new ArchiveEntityProperty("create-time", Long.toString(create)));

		long modify = attributes.lastModifiedTime().toMillis();
		properties.add(new ArchiveEntityProperty("modify-time", Long.toString(modify)));

		long access = attributes.lastAccessTime().toMillis();
		properties.add(new ArchiveEntityProperty("access-time", Long.toString(access)));

		ArchiveEntityProperty[] ptsArr = properties.toArray(new ArchiveEntityProperty[0]);
		if (Files.isDirectory(path))
			return new DirectoryEntity(localName, ptsArr);
		else
			return new FileEntity(localName, Files.newByteChannel(path), ptsArr);
	}
}
