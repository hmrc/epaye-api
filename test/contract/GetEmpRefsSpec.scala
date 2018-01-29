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
import play.api.libs.json.Json
import play.api.routing.Router
import uk.gov.hmrc.epayeapi.models.in.EpayeEmpRefsResponse
import uk.gov.hmrc.epayeapi.router.RoutesProvider
import uk.gov.hmrc.epayeapi.models.Formats._

import scala.io.Source

class GetEmpRefsSpec
  extends WordSpec
  with Matchers
  with WSClientSetup
  with WiremockSetup
  with EmpRefGenerator
  with RestAssertions {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder().overrides(bind[Router].toProvider[RoutesProvider]).build()

  "/organisations/epaye" should {
    "return a response body that conforms with the EmpRefs schema" in new Setup {
      given()
        .client.isAuthorized
        .and().epayeEmpRefsEndpointReturns(empRefJson)
        .when()
        .get(url).withAuthHeader()
        .thenAssertThat()
        .bodyIsOfSchema(empRefSchemaPath)
    }
  }

  "The provided example for empRefs" should {
    "conform to the schema" in new Setup {
      val report = Schema(empRefSchemaPath).validate(empRefExample)
      withClue(report.toString) { report.isSuccess shouldBe true }
    }
  }

  trait Setup {
    val url                      = baseUrl
    val apiBaseUrl               = app.configuration.underlying.getString("api.baseUrl")
    val empRefs                  = for (_ <- 1 to 5) yield getEmpRef
    val empRefSchemaPath: String = getUriString("/public/api/conf/1.0/schemas/Epaye.get.schema.array.json")
    val empRefExample   : String = getResourceAsString("/public/api/conf/1.0/examples/Epaye.get.array.json")
    lazy val empRefJson = Json.toJson(EpayeEmpRefsResponse(empRefs)).toString()

    def getUriString(name: String): String =
      getClass.getResource(name).toURI.toString

    def getResourceAsString(name: String): String =
      Source.fromURL(getClass.getResource(name), "utf-8").mkString("")
  }
}
