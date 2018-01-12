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

package integration

import common._
import contract._
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.routing.Router
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.router.RoutesProvider

class GetAnnualStatementSpec extends WordSpec
  with Matchers
  with WSClientSetup
  with WiremockSetup
  with EmpRefGenerator
  with RestAssertions {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder().overrides(bind[Router].toProvider[RoutesProvider]).build()

  "AnnualStatement API" should {
    "return a statement containing EYU data" in {
      val apiBaseUrl = app.configuration.underlying.getString("api.baseUrl")

      val empRef = EmpRefGenerator.getEmpRef

      val annualStatementUrl =
        s"$baseUrl/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2017-18"

      given()
        .clientWith(empRef).isAuthorized
        .and().epayeAnnualStatementReturns(
          Fixtures.epayeAnnualStatement

        )
        .when()
        .get(annualStatementUrl).withAuthHeader()
        .thenAssertThat()
        .bodyIsOfJson(
          Fixtures.expectedAnnualStatementJson(apiBaseUrl, empRef)
        )
    }
  }
}
