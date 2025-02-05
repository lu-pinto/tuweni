// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static org.apache.tuweni.bytes.v2.Checks.checkArgument;
import static org.apache.tuweni.bytes.v2.Checks.checkElementIndex;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;

import io.vertx.core.buffer.Buffer;

class ArrayWrappingBytes extends Bytes {

  protected final byte[] bytes;
  protected final int offset;
  protected final int length;

  ArrayWrappingBytes(byte[] bytes) {
    this(bytes, 0, bytes.length);
  }

  ArrayWrappingBytes(byte[] bytes, int offset, int length) {
    checkArgument(length >= 0, "Invalid negative length");
    if (bytes.length > 0) {
      checkElementIndex(offset, bytes.length);
    }
    checkArgument(
        offset + length <= bytes.length,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        bytes.length - offset,
        offset);

    this.bytes = bytes;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public int size() {
    return length;
  }

  @Override
  public byte get(int i) {
    // Check bounds because while the array access would throw, the error message would be confusing
    // for the caller.
    checkElementIndex(i, size());
    return bytes[offset + i];
  }

  @Override
  public Bytes slice(int i, int length) {
    if (i == 0 && length == this.length) {
      return this;
    }
    if (length == 0) {
      return Bytes.EMPTY;
    }

    checkElementIndex(i, this.length);
    checkArgument(
        i + length <= this.length,
        "Provided length %s is too big: the value has size %s and has only %s bytes from %s",
        length,
        this.length,
        this.length - i,
        i);

    return new ArrayWrappingBytes(bytes, offset + i, length);
  }

//  TODO: Finish MutableBytes
//  @Override
//  public MutableBytes mutableCopy() {
//    return new MutableArrayWrappingBytes(toArray());
//  }

  @Override
  public int commonPrefixLength(Bytes other) {
    if (!(other instanceof ArrayWrappingBytes o)) {
      return super.commonPrefixLength(other);
    }
    int i = 0;
    while (i < length && i < o.length && bytes[offset + i] == o.bytes[o.offset + i]) {
      i++;
    }
    return i;
  }

  @Override
  public void update(MessageDigest digest) {
    digest.update(bytes, offset, length);
  }

//  TODO: Finish MutableBytes
//  @Override
//  public void copyTo(MutableBytes destination, int destinationOffset) {
//    if (!(destination instanceof MutableArrayWrappingBytes d)) {
//      super.copyTo(destination, destinationOffset);
//      return;
//    }
//
//    int size = size();
//    if (size == 0) {
//      return;
//    }
//
//    checkElementIndex(destinationOffset, destination.size());
//    checkArgument(
//        destination.size() - destinationOffset >= size,
//        "Cannot copy %s bytes, destination has only %s bytes from index %s",
//        size,
//        destination.size() - destinationOffset,
//        destinationOffset);
//
//      System.arraycopy(bytes, offset, d.bytes, d.offset + destinationOffset, size);
//  }

  @Override
  public void appendTo(ByteBuffer byteBuffer) {
    byteBuffer.put(bytes, offset, length);
  }

  @Override
  public void appendTo(Buffer buffer) {
    buffer.appendBytes(bytes, offset, length);
  }

  @Override
  public byte[] toArray() {
    return Arrays.copyOfRange(bytes, offset, offset + length);
  }

  @Override
  public byte[] toArrayUnsafe() {
    if (offset == 0 && length == bytes.length) {
      return bytes;
    }
    return toArray();
  }
}
