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


import io.Buffers;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * ArchiveEntityProcessor is an extension of {@link ArchiveEntityDecorator} class, that provides encoding and decoding
 * of data accessed through {@link ArchiveEntityDecorator#getContent(ByteBuffer)}. Encoding and decoding happens
 * unbeknownst to the client via invocation of {@link #getContent(ByteBuffer)}, thus encapsulating the entire process.
 * The concrete implementation must implement the encoding and decoding methods, so that decode(encode(x)) == x.<br><br>
 *
 * ArchiveEntityProcessor initializes and stores two buffers: one containing the bytes read from
 * getComponent().getContent(), the second one contains the bytes provided by {@link #encode(ByteBuffer, ByteBuffer)}
 * or {@link #decode(ByteBuffer, ByteBuffer)}, which were processed but not yet sent to the client via
 * {@link #getContent(ByteBuffer)} due to the buffer size difference.<br>
 * The concrete implementation must specify the lengths of these two buffers through
 * {@link #getPreferredUnprocessedWindowSize()} and {@link #getPreferredProcessedWindowSize()}. ArchiveEntityProcessor
 * guarantees that the first buffer will be filled unless there is no data left in the underlying entity, but
 * the concrete implementation doesn't have to process all bytes it gets, allowing it to implement
 * a silent header.<br><br>
 *
 * The silent header is a set of bytes appended to the end of the data stream of the underlying archive entity and known
 * only to the concrete implementation. In {@link AESEntity} a silent header is a message digest ( hash ) at the end of
 * the encoded data. {@link AESEntity#encode(ByteBuffer, ByteBuffer)} encrypts the incoming data and when there is no
 * data left in the underlying entity it simply attaches the computed message digest of the unencrypted data to the end
 * of the output buffer.<br>
 * When the data is decrypted using {@link AESEntity#decode(ByteBuffer, ByteBuffer)} it doesn't process the last
 * 32 bytes ( the length of SHA-256 ) immediately but instead cashes them. The next time decode() is called it processes
 * the cashed 32 bytes alongside the new ones, but it again cashes the last 32 bytes, unless the underlying entity has
 * no data left. When it happens it compares the computed hash with the attached one and if they don't match it throws
 * an exception that the data has been corrupted.<br>
 * As you can see the fact that {@link AESEntity} modified the data in encode() and decode() beyond simple encryption
 * and decryption is hidden from the client.<br><br>
 *
 * The structure of the silent header and the necessity to implement it at all is up to the concrete implementation.
 */
public abstract class ArchiveEntityProcessor extends ArchiveEntityDecorator {

	private boolean encode = true;

	private final ByteBuffer read = ByteBuffer.allocate(getPreferredUnprocessedWindowSize());

	private final ByteBuffer processed = ByteBuffer.allocate(getPreferredProcessedWindowSize());

	/**
	 * Encode the information passed through the input buffer and write it to the output buffer.<br>
	 * It's guaranteed that the length of the input buffer is getPreferredUnprocessedWindowSize() and the length of
	 * the output buffer is getPreferredProcessedWindowSize() and that the input buffer is filled unless the underlying
	 * stream has no data left.
	 * @param in the bytes read from the underlying entity
	 * @param out the buffer to write the encoded data to
	 * @return the number of encoded data in bytes
	 * @throws IOException if some I/O errors occur
	 */
	protected abstract int encode(ByteBuffer in, ByteBuffer out) throws IOException;

	/**
	 * Decode the information passed through the input buffer and write it to the output buffer.<br>
	 * It's guaranteed that the length of the input buffer is getPreferredUnprocessedWindowSize() and the length of
	 * the output buffer is getPreferredProcessedWindowSize() and that the in buffer is filled unless the underlying
	 * stream has no data left.
	 * @param in the bytes read from the underlying stream
	 * @param out the buffer to write the decoded data to
	 * @return the number of decoded data in bytes
	 * @throws IOException if some I/O errors occur
	 */
	protected abstract int decode(ByteBuffer in, ByteBuffer out) throws IOException;

	/**
	 * The size of the first buffer passed to encode() and decode(). The size is accessed when ArchiveEntityProcessor is
	 * initialized and doesn't change after.
	 * @return the size of the first buffer passed to encode() and decode()
	 */
	protected abstract int getPreferredUnprocessedWindowSize();

	/**
	 * The size of the second buffer passed to encode() and decode(). The size is accessed when ArchiveEntityProcessor
	 * is initialized and doesn't change after.
	 * @return the size of the second buffer passed to encode() and decode()
	 */
	protected abstract int getPreferredProcessedWindowSize();

	/**
	 * Sets the encoding or decoding of the data of the underlying entity.
	 * @param encode true if encode, false if decode
	 */
	public void setEncode(boolean encode) {
		this.encode = encode;
	}

	/**
	 * Returns true if the data of the underlying entity is to be encoded, otherwise returns false.
	 * @return true if the data of the underlying entity is to be encoded, otherwise returns false.
	 */
	public boolean getEncoded() {
		return encode;
	}

	protected ArchiveEntityProcessor() {
		processed.flip();
	}

	private int process(ByteBuffer in, ByteBuffer out) throws IOException {
		assert in != null && out != null;

		if (encode) return encode(in, out);
		else return decode(in, out);
	}

	/**
	 * Encodes or decodes the data of the underlying entity depending on the {@link #setEncode(boolean)} parameter.
	 * @param byteBuffer the buffer the data is written to
	 * @return the number of bytes written to the buffer, or -1 if the end of the stream was reached
	 * @throws IOException if some I/O errors occur
	 */
	@Override
	public int getContent(ByteBuffer byteBuffer) throws IOException {
		assert byteBuffer != null;

		int transferred = 0;
		if (processed.hasRemaining()) {
			transferred += Buffers.transfer(processed, byteBuffer);
			if (!byteBuffer.hasRemaining()) {
				return transferred;
			}
		}

		while (byteBuffer.hasRemaining() && super.getContent(read) > 0) {
			processed.clear();
			read.flip();
			process(read, processed);
			processed.flip();
			transferred += Buffers.transfer(processed, byteBuffer);
			read.clear();
		}

		if (transferred == 0 && !hasRemainingContent()) return -1;
		else return transferred;
	}

	@Override
	public boolean hasRemainingContent() throws IOException {
		return super.hasRemainingContent() || processed.hasRemaining();
	}

	/**
	 * Forbids to set null.
	 * @param archiveEntity an ArchiveEntity object
	 */
	@Override
	public void setComponent(ArchiveEntity archiveEntity) {
		assert archiveEntity != null;

		super.setComponent(archiveEntity);
	}
}
