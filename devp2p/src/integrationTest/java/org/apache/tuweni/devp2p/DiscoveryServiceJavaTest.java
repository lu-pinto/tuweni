// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.devp2p;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tuweni.concurrent.AsyncCompletion;
import org.apache.tuweni.concurrent.AsyncResult;
import org.apache.tuweni.crypto.SECP256K1;
import org.apache.tuweni.junit.BouncyCastleExtension;
import org.apache.tuweni.junit.VertxExtension;
import org.apache.tuweni.junit.VertxInstance;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

@Timeout(10)
@ExtendWith({BouncyCastleExtension.class, VertxExtension.class})
class DiscoveryServiceJavaTest {

  @Test
  void setUpAndShutDownAsync(@VertxInstance Vertx vertx) throws Exception {
    DiscoveryService service =
        DiscoveryService.Companion.open(vertx, SECP256K1.KeyPair.random(), 0, "127.0.0.1");
    service.awaitBootstrapAsync().join();
    AsyncCompletion completion = service.shutdownAsync();
    completion.join();
    assertTrue(completion.isDone());
  }

  @Test
  void lookupAsync(@VertxInstance Vertx vertx) throws Exception {
    DiscoveryService service =
        DiscoveryService.Companion.open(vertx, SECP256K1.KeyPair.random(), 0, "127.0.0.1");
    service.awaitBootstrapAsync().join();
    AsyncResult<List<Peer>> result = service.lookupAsync(SECP256K1.KeyPair.random().publicKey());
    List<Peer> peers = result.get();
    service.shutdownAsync().join();
    assertTrue(peers != null && peers.isEmpty());
  }

  @Test
  void managePeerRepository(@VertxInstance Vertx vertx) throws Exception {
    SECP256K1.KeyPair peerKeyPair = SECP256K1.KeyPair.random();
    EphemeralPeerRepository repository = new EphemeralPeerRepository();
    DiscoveryService service =
        DiscoveryService.Companion.open(
            vertx,
            SECP256K1.KeyPair.random(),
            32456,
            "127.0.0.1",
            1,
            emptyMap(),
            Collections.singletonList(
                URI.create(
                    "enode://" + peerKeyPair.publicKey().toHexString() + "@127.0.0.1:10000")),
            repository);
    AsyncResult<Peer> result =
        repository.getAsync(
            URI.create("enode://" + peerKeyPair.publicKey().toHexString() + "@127.0.0.1:10000"));
    assertEquals(peerKeyPair.publicKey(), Objects.requireNonNull(result.get()).getNodeId());
    AsyncResult<Peer> byURIString =
        repository.getAsync(
            "enode://" + peerKeyPair.publicKey().toHexString() + "@127.0.0.1:10000");
    assertEquals(peerKeyPair.publicKey(), Objects.requireNonNull(byURIString.get()).getNodeId());
    service.shutdownAsync().join();
  }
}
