// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.benchmark;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(value = Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1)
@OutputTimeUnit(value = TimeUnit.MILLISECONDS)
public class BytesMegamorphicBenchmark {
    private static final int N = 4;
    private static final int FACTOR = 1000000;
    private static final Random RANDOM = new Random(23L);
    org.apache.tuweni.bytes.Bytes[] bytesV1;
    org.apache.tuweni.bytes.v2.Bytes[] bytesV2;
    Version version;

    @Param({"V1", "V2"})
    private String versionParam;

    @Param({"mono", "mega"})
    private String mode;

    @Setup
    public void setup() {
        version = Version.valueOf(versionParam);
        bytesV1 = new org.apache.tuweni.bytes.Bytes[N * FACTOR];
        for (int i = 0; i < N * FACTOR; i += N) {
            bytesV1[i] = org.apache.tuweni.bytes.Bytes.wrap(getBytes(32));
            bytesV1[i + 1] = "mega".equals(mode) ? org.apache.tuweni.bytes.Bytes.wrap(getBytes(48)) : org.apache.tuweni.bytes.Bytes.wrap(getBytes(32));
            bytesV1[i + 2] = "mega".equals(mode) ? org.apache.tuweni.bytes.Bytes.repeat((byte) 0x09, 16) : org.apache.tuweni.bytes.Bytes.wrap(getBytes(32));
            bytesV1[i + 3] = "mega".equals(mode) ? org.apache.tuweni.bytes.Bytes.wrap(bytesV1[i], bytesV1[i+1]) : org.apache.tuweni.bytes.Bytes.wrap(getBytes(32));
        }
        bytesV2 = new org.apache.tuweni.bytes.v2.Bytes[N * FACTOR];
        for (int i = 0; i < N * FACTOR; i += N) {
            bytesV2[i] = org.apache.tuweni.bytes.v2.Bytes.wrap(getBytes(32));
            bytesV2[i + 1] = "mega".equals(mode) ? org.apache.tuweni.bytes.v2.Bytes.wrap(getBytes(48)) : org.apache.tuweni.bytes.v2.Bytes.wrap(getBytes(32));
            bytesV2[i + 2] = "mega".equals(mode) ? org.apache.tuweni.bytes.v2.Bytes.repeat((byte) 0x09, 16) : org.apache.tuweni.bytes.v2.Bytes.wrap(getBytes(32));
            bytesV2[i + 3] = "mega".equals(mode) ? org.apache.tuweni.bytes.v2.Bytes.wrap(bytesV2[i], bytesV2[i+1]) : org.apache.tuweni.bytes.v2.Bytes.wrap(getBytes(32));
        }
    }

    private static byte[] getBytes(final int size) {
        byte[] b = new byte[size];
        RANDOM.nextBytes(b);
        return b;
    }

    @Benchmark
    public void test() {
        switch (version) {
            case V1 -> {
                for (org.apache.tuweni.bytes.Bytes b : bytesV1) {
                    b.get(1);
                }
            }
            case V2 -> {
                for (org.apache.tuweni.bytes.v2.Bytes b : bytesV2) {
                    b.get(1);
                }
            }
        }
    }

    enum Version {
        V1, V2;
    }
}
