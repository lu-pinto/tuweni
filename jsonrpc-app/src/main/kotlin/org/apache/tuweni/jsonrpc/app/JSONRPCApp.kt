/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tuweni.jsonrpc.app

import io.vertx.core.Vertx
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.apache.tuweni.eth.JSONRPCRequest
import org.apache.tuweni.eth.JSONRPCResponse
import org.apache.tuweni.jsonrpc.JSONRPCServer
import org.apache.tuweni.metrics.MetricsService
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.security.Security
import java.util.concurrent.Executors

val logger = LoggerFactory.getLogger(JSONRPCApp::class.java)

/**
 * Application running a JSON-RPC service that is able to connect to clients, get information, cache and distribute it.
 */
object JSONRPCApp {

  @JvmStatic
  fun main(args: Array<String>) {
    val configFile = Paths.get(if (args.isNotEmpty()) args[0] else "config.toml")
    Security.addProvider(BouncyCastleProvider())
    val vertx = Vertx.vertx()
    val config = JSONRPCConfig(configFile)
    if (config.config.hasErrors()) {
      for (error in config.config.errors()) {
        println(error.message)
      }
      System.exit(1)
    }
    val app = JSONRPCApplication(vertx, config)
    app.run()
  }
}

class JSONRPCApplication(
  val vertx: Vertx,
  val config: JSONRPCConfig
) {

  private val metricsService = MetricsService(
    "json-rpc",
    port = config.metricsPort(),
    networkInterface = config.metricsNetworkInterface(),
    enableGrpcPush = config.metricsGrpcPushEnabled(),
    enablePrometheus = config.metricsPrometheusEnabled()
  )

  fun run() {
    val server = JSONRPCServer(
      vertx, config.port(), config.networkInterface(), this::handleRequest,
      Executors.newFixedThreadPool(
        config.numberOfThreads()
      ) {
        val thread = Thread("jsonrpc")
        thread.isDaemon = true
        thread
      }.asCoroutineDispatcher()
    )
    Runtime.getRuntime().addShutdownHook(
      Thread() {
        runBlocking {
          server.stop().await()
        }
      }
    )
    runBlocking {
      server.start().await()
      logger.info("JSON-RPC server started")
    }
  }

  private fun handleRequest(request: JSONRPCRequest): JSONRPCResponse {
    logger.info("Received request {}", request)
    return JSONRPCResponse(1) // TODO implement
  }
}
