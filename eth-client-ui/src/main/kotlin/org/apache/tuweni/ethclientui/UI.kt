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
package org.apache.tuweni.ethclientui

import org.apache.tuweni.ethclient.EthereumClient
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.util.resource.Resource
import org.glassfish.jersey.servlet.ServletContainer

class UI(
  val port: Int = 0,
  val networkInterface: String = "127.0.0.1",
  val path: String = "/",
  val client: EthereumClient
) {

  private var server: Server? = null

  var actualPort: Int? = null

  fun start() {
    val newServer = Server(port)

    val ctx = ServletContextHandler(ServletContextHandler.NO_SESSIONS)

    ctx.contextPath = path
    newServer.handler = ctx

    val serHol = ctx.addServlet(ServletContainer::class.java, "/rest/*")
    serHol.initOrder = 1
    serHol.setInitParameter(
      "jersey.config.server.provider.packages",
      "org.apache.tuweni.ethclientui"
    )

    ctx.setBaseResource(Resource.newResource(UI::class.java.getResource("/webapp")))
    val staticContent = ctx.addServlet(DefaultServlet::class.java, "/*")
    ctx.setWelcomeFiles(arrayOf("index.html"))
    staticContent.initOrder = 10

    newServer.stopAtShutdown = true
    newServer.start()
    serHol.servlet.servletConfig.servletContext.setAttribute("ethclient", client)
    server = newServer
    actualPort = newServer.uri.port
  }

  fun stop() {
    server?.stop()
  }
}
