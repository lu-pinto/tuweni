package org.benchmark;

import org.apache.tuweni.v2.bytes.Bytes;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(value = Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(value = TimeUnit.NANOSECONDS)
public class ByteBufferBenchmark {
    private static final int N = 4;
    private static final int FACTOR = 1_000;
    private static final Random RANDOM = new Random(23L);
    ByteBuffer[] byteBuffers;
    ByteBuffer[] byteBuffersDup;
    private int index;
    private static final int MAX_INDEX = 10;

    public enum MODE {
        MONO_OFF_HEAP,
        MONO_ON_HEAP_FULL_ARRAY_ACCESS,
        MONO_ON_HEAP_ARRAY_INDEXING;
    }

    @Param
    public MODE mode;

    @Setup
    public void setup() {
        byteBuffers = new ByteBuffer[N * FACTOR];
        byteBuffersDup = new ByteBuffer[N * FACTOR];
        for (int i = 0; i < N * FACTOR; i += N) {
            byteBuffers[i] = createByteBuffer(1024);
            byteBuffersDup[i] = clone(byteBuffers[i]);
            byteBuffers[i + 1] = createByteBuffer(1024);
            byteBuffersDup[i + 1] = clone(byteBuffers[i + 1]);
            byteBuffers[i + 2] = createByteBuffer(1024);
            byteBuffersDup[i + 2] = clone(byteBuffers[i + 2]);
            byteBuffers[i + 3] = createByteBuffer(1024);
            byteBuffersDup[i + 3] = clone(byteBuffers[i + 3]);
        }
    }

    private ByteBuffer clone(final ByteBuffer byteBuffer) {
        return switch (mode) {
            case MONO_OFF_HEAP -> {
                final byte[] newArray = new byte[byteBuffer.limit()];
                byteBuffer.get(newArray).position(0);
                ByteBuffer buf = ByteBuffer.allocateDirect(newArray.length);
                yield buf.put(newArray).position(0);
            }
            case MONO_ON_HEAP_ARRAY_INDEXING, MONO_ON_HEAP_FULL_ARRAY_ACCESS -> {
                final byte[] newArray = new byte[byteBuffer.limit()];
                byteBuffer.get(newArray).position(0);
                yield ByteBuffer.wrap(newArray).position(0);
            }
        };
    }

    private ByteBuffer createByteBuffer(final int size) {
        return switch (mode) {
            case MONO_OFF_HEAP -> {
                ByteBuffer buf = ByteBuffer.allocateDirect(size);
                yield buf.put(getBytes(size)).position(0);

            }
            case MONO_ON_HEAP_ARRAY_INDEXING, MONO_ON_HEAP_FULL_ARRAY_ACCESS -> ByteBuffer.wrap(getBytes(size)).position(0);
        };
    }

    private static byte[] getBytes(final int size) {
        byte[] b = new byte[size];
        RANDOM.nextBytes(b);
        return b;
    }

    @Benchmark
    @OperationsPerInvocation(N * FACTOR)
    public void slice() {
        assert mode != MODE.MONO_ON_HEAP_ARRAY_INDEXING;
        for (ByteBuffer b : byteBuffers) {
            b.slice(index++, index * 2);
            index %= MAX_INDEX;
        }
    }

    @Benchmark
    @OperationsPerInvocation(N * FACTOR)
    public void toHex(Blackhole bh) {
        for (ByteBuffer b : byteBuffers) {
            bh.consume(toHex(b));
        }
    }

    @Benchmark
    @OperationsPerInvocation(N * FACTOR)
    public void getInt(Blackhole bh) {
        assert mode != MODE.MONO_ON_HEAP_ARRAY_INDEXING;
        for (ByteBuffer b : byteBuffers) {
            bh.consume(b.getInt(index++));
            index %= MAX_INDEX;
        }
    }

    @Benchmark
    @OperationsPerInvocation(N * FACTOR)
    public void equals(Blackhole bh) {
        assert mode != MODE.MONO_ON_HEAP_ARRAY_INDEXING;
        for (int i = 0; i < byteBuffers.length; i++) {
            bh.consume(byteBuffers[i].equals(byteBuffersDup[i]));
        }
    }

    private String toHex(ByteBuffer buf) {
        return switch (mode) {
            case MONO_OFF_HEAP, MONO_ON_HEAP_ARRAY_INDEXING -> toHexFromBuffer(buf);
            case MONO_ON_HEAP_FULL_ARRAY_ACCESS -> toHexFromArray(buf);
        };
    }

    private static String toHexFromBuffer(ByteBuffer buf) {
        final int offset = 2;
        int resultSize = (buf.limit() * 2) + offset;
        char[] result = new char[resultSize];
        result[0] = '0';
        result[1] = 'x';

        for (int i = 0; i < buf.limit(); i++) {
            byte b = buf.get(i);
            int pos = i * 2;
            result[pos + offset] = Bytes.HEX_CODE_AS_STRING.charAt(b >> 4 & 15);
            result[pos + offset + 1] = Bytes.HEX_CODE_AS_STRING.charAt(b & 15);
        }
        return new String(result);
    }

    private static String toHexFromArray(final ByteBuffer buf) {
        final int offset = 2;
        final int arrayOffset = buf.arrayOffset();
        final byte[] array = buf.array();
        final int length = buf.limit();
        int resultSize = (length * 2) + offset;
        char[] result = new char[resultSize];
        result[0] = '0';
        result[1] = 'x';

        for (int i = 0; i < length; i++) {
            byte b = array[i + arrayOffset];
            int pos = i * 2;
            result[pos + offset] = Bytes.HEX_CODE_AS_STRING.charAt(b >> 4 & 15);
            result[pos + offset + 1] = Bytes.HEX_CODE_AS_STRING.charAt(b & 15);
        }
        return new String(result);
    }
}
