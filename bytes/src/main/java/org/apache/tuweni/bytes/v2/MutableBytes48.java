// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static org.apache.tuweni.bytes.v2.Bytes48.SIZE;
import static org.apache.tuweni.bytes.v2.Checks.checkArgument;
import static org.apache.tuweni.bytes.v2.Checks.checkNotNull;

/** A mutable {@link Bytes48}, that is a mutable {@link Bytes} value of exactly 48 bytes. */
public class MutableBytes48 extends MutableBytes {

  /**
   * Create a new mutable 48 bytes value.
   *
   * @return A newly allocated {@link MutableBytes} value.
   */
  public static MutableBytes48 create() {
    return new MutableArrayWrappingBytes48(new byte[SIZE]);
  }

  /**
   * Wrap a 48 bytes array as a mutable 48 bytes value.
   *
   * @param value The value to wrap.
   * @return A {@link MutableBytes48} wrapping {@code value}.
   * @throws IllegalArgumentException if {@code value.length != 48}.
   */
  public static MutableBytes48 wrap(byte[] value) {
    checkNotNull(value);
    return new MutableArrayWrappingBytes48(value);
  }

  /**
   * Wrap a the provided array as a {@link MutableBytes48}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * within the wrapped parts will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, i).get(0) == value[i]}.
   * @return A {@link MutableBytes48} that exposes the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + 48} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.length > 0 && offset >=
   *     value.length)}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + 48 > value.length}.
   */
  public static MutableBytes48 wrap(byte[] value, int offset) {
    checkNotNull(value);
    return new MutableArrayWrappingBytes48(value, offset);
  }

  /**
   * Wrap a the provided value, which must be of size 48, as a {@link MutableBytes48}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @return A {@link MutableBytes48} that exposes the bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() != 48}.
   */
  public static MutableBytes48 wrap(MutableBytes value) {
    checkNotNull(value);
    if (value instanceof MutableBytes48 bytes48) {
      return bytes48;
    }
    return DelegatingMutableBytes48.delegateTo(value);
  }

  /**
   * Wrap a slice/sub-part of the provided value as a {@link MutableBytes48}.
   *
   * <p>Note that the value is not copied, and thus any future update to {@code value} within the
   * wrapped parts will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, i).get(0) == value.get(i)}.
   * @return A {@link Bytes48} that exposes the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + 48} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.size() > 0 && offset >=
   *     value.size())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + 48 > value.size()}.
   */
  public static MutableBytes48 wrap(MutableBytes value, int offset) {
    checkNotNull(value);
    if (value instanceof MutableBytes48 bytes48) {
      return bytes48;
    }
    MutableBytes slice = value.mutableSlice(offset, SIZE);
    if (slice instanceof MutableBytes48 bytes48) {
      return bytes48;
    }
    return DelegatingMutableBytes48.delegateTo(slice);
  }

  /**
   * Left pad a {@link Bytes} value with zero bytes to create a {@link Bytes48}.
   *
   * @param value The bytes value pad.
   * @return A {@link Bytes48} that exposes the left-padded bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() > 48}.
   */
  public static MutableBytes48 leftPad(MutableBytes value) {
    checkNotNull(value);
    if (value instanceof MutableBytes48 bytes48) {
      return bytes48;
    }
    checkArgument(value.size() <= SIZE, "Expected at most %s bytes but got %s", SIZE, value.size());
    MutableBytes48 result = create();
    value.copyTo(result, SIZE - value.size());
    return result;
  }

  /**
   * Right pad a {@link Bytes} value with zero bytes to create a {@link Bytes48}.
   *
   * @param value The bytes value pad.
   * @return A {@link Bytes48} that exposes the rightw-padded bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() > 48}.
   */
  public static MutableBytes48 rightPad(MutableBytes value) {
    checkNotNull(value);
    if (value instanceof MutableBytes48 bytes48) {
      return bytes48;
    }
    checkArgument(value.size() <= SIZE, "Expected at most %s bytes but got %s", SIZE, value.size());
    MutableBytes48 result = create();
    value.copyTo(result, 0);
    return result;
  }

  /**
   * Return a bit-wise AND of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise AND.
   */
  public Bytes48 and(Bytes48 other) {
    return and(other, MutableBytes48.create());
  }

  /**
   * Return a bit-wise OR of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise OR.
   */
  public Bytes48 or(Bytes48 other) {
    return or(other, MutableBytes48.create());
  }

  /**
   * Return a bit-wise XOR of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise XOR.
   */
  public Bytes48 xor(Bytes48 other) {
    return xor(other, MutableBytes48.create());
  }

  public Bytes48 not() {
    return not(MutableBytes48.create());
  }

  public Bytes48 shiftRight(int distance) {
    return shiftRight(distance, MutableBytes48.create());
  }

  public Bytes48 shiftLeft(int distance) {
    return shiftLeft(distance, MutableBytes48.create());
  }
}
