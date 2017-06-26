/*
 * Copyright 2017 HM Revenue & Customs
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

package unit.config

import play.api.{Application, Configuration, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.epayeapi.config.AppContext
import unit.AppSpec

class AppContextSpec extends AppSpec {

  def app: Application = GuiceApplicationBuilder()
    .configure("run.mode" -> "Prod")
    .in(Mode.Prod)
    .build()

  "AppContext" should {
    "return the right service locator" in new App(app) {
      val context = inject[AppContext]

      context.serviceLocatorUrl shouldBe "http://service-locator.protected.mdtp:80"
    }
  }
}
