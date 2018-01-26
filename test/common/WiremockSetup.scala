/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package common

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest._

trait WiremockSetup extends BeforeAndAfterEach with BeforeAndAfterAll { self: Suite =>

  lazy val WIREMOCK_PORT: Int = 22222

  val wiremockBaseUrl: String = s"http://localhost:$WIREMOCK_PORT"
  val wireMockServer =
    new WireMockServer(wireMockConfig()
                         .port(WIREMOCK_PORT)
                         .notifier(new Slf4jNotifier(true)))



  override def beforeAll(): Unit = {
    wireMockServer.start()
    WireMock.configureFor("localhost", WIREMOCK_PORT)
  }

  override def beforeEach(): Unit = {
    WireMock.reset()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }
}


