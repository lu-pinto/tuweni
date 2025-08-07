// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.apache.tuweni.v2.bytes.Utils.checkArgument;
import static org.apache.tuweni.v2.bytes.Utils.checkElementIndex;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;

import io.vertx.core.buffer.Buffer;

class ArrayWrappingBytes extends Bytes {

  protected final byte[] bytes;
  protected final int offset;

  ArrayWrappingBytes(byte[] bytes) {
    this(bytes, 0, bytes.length);
  }

  ArrayWrappingBytes(byte[] bytes, int offset, int length) {
    super(length);
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
    if (i == 0 && length == size()) {
      return this;
    }
    if (length == 0) {
      return EMPTY;
    }

    checkElementIndex(i, size());
    checkArgument(
        i + length <= size(),
        "Provided length %s is too big: the value has size %s and has only %s bytes from %s",
        length,
        size(),
        size() - i,
        i);

    return new ArrayWrappingBytes(bytes, offset + i, length);
  }

  @Override
  public int commonPrefixLength(Bytes other) {
    if (!(other instanceof ArrayWrappingBytes o)) {
      return super.commonPrefixLength(other);
    }
    int i = 0;
    while (i < size() && i < o.size() && bytes[offset + i] == o.bytes[o.offset + i]) {
      i++;
    }
    return i;
  }

  @Override
  public void update(MessageDigest digest) {
    digest.update(bytes, offset, size());
  }

  @Override
  public void appendTo(ByteBuffer byteBuffer) {
    byteBuffer.put(bytes, offset, size());
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromArray(bytes, offset, size());
  }

  @Override
  public void appendTo(Buffer buffer) {
    buffer.appendBytes(bytes, offset, size());
  }

  @Override
  public byte[] toArrayUnsafe() {
    if (offset == 0 && size() == bytes.length) {
      return bytes;
    }
    return Arrays.copyOfRange(bytes, offset, offset + size());
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    Utils.and(this.bytes, this.offset, bytesArray, offset, length);
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    Utils.or(this.bytes, this.offset, bytesArray, offset, length);
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    Utils.xor(this.bytes, this.offset, bytesArray, offset, length);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Bytes other)) {
      return false;
    }

    if (this.size() != other.size()) {
      return false;
    }

    for (int i = 0; i < size(); i++) {
      if (bytes[i + offset] != other.get(i)) {
        return false;
      }
    }

    return true;
  }
}
