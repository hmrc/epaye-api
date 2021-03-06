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

package contract

import common._
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.routing.Router
import uk.gov.hmrc.epayeapi.models.TaxYear
import uk.gov.hmrc.epayeapi.router.RoutesProvider

class GetStatementsSpec
  extends WordSpec
  with Matchers
  with WSClientSetup
  with WiremockSetup
  with EmpRefGenerator
  with RestAssertions {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder().overrides(bind[Router].toProvider[RoutesProvider]).build()

  "/organisations/epaye/{ton}/{tor}/statements" should {

    "return a response body that conforms to the Statements schema" in {
      val empRef = randomEmpRef()
      val taxYear = TaxYear(2017)

      val statementsUrl =
        s"$baseUrl/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements"

      val statementsSchemaPath = s"${app.path.toURI}/resources/public/api/conf/1.0/schemas/Statements.schema.json"

      given()
        .clientWith(empRef).isAuthorized
        .and().epayeMasterDataReturns(Fixtures.epayeMasterData(empRef, taxYear))
        .when
        .get(statementsUrl).withAuthHeader()
        .thenAssertThat()
        .bodyIsOfSchema(statementsSchemaPath)
    }
  }
}
