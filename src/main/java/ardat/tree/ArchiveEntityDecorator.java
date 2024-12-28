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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ArchiveEntityDecorator is an abstract class that stores an ArchiveEntity object, replicates the same interface, and
 * forwards the interface invocations to the component.
 * ArchiveEntityDecorator is a Decorator participant of the Decorator pattern.
 */
public abstract class ArchiveEntityDecorator extends ArchiveEntity {

	private ArchiveEntity component = null;

	/**
	 * Returns the ArchiveEntity object.
	 * @return the ArchiveEntity object
	 */
	public ArchiveEntity getComponent() {
		return component;
	}

	/**
	 * Sets an ArchiveEntity object.
	 * @param archiveEntity an ArchiveEntity object
	 */
	public void setComponent(ArchiveEntity archiveEntity) {
		component = archiveEntity;
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	protected void setParent(ArchiveEntity entity) {
		getComponent().setParent(entity);
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	protected ArchiveEntity getParent() {
		return getComponent().getParent();
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public ArchiveEntity[] getChildren() {
		return getComponent().getChildren();
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public ArchiveEntity[] addChildren(ArchiveEntity... entity) {
		return getComponent().addChildren(entity);
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public ArchiveEntity removeChild(ArchiveEntity entity) {
		return getComponent().removeChild(entity);
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public ArchiveEntityProperty[] getProperties() {
		return getComponent().getProperties();
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public int getContent(ByteBuffer byteBuffer) throws IOException {
		return getComponent().getContent(byteBuffer);
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public boolean hasRemainingContent() throws IOException {
		return getComponent().hasRemainingContent();
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public String[] getName() {
		return getComponent().getName();
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public boolean isLeaf() {
		return getComponent().isLeaf();
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public boolean isOpened() {
		return getComponent().isOpened();
	}

	/**
	 * Forwards the invocation to the component.
	 */
	@Override
	public void close() throws IOException {
		getComponent().close();
	}
}
