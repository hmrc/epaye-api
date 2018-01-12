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

package uk.gov.hmrc.epayeapi.controllers

import akka.util.ByteString
import org.mockito.Matchers._
import org.mockito.Mockito.reset
import org.mockito.{Matchers, Mockito}
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.Accumulator
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.config.WSHttp
import uk.gov.hmrc.epayeapi.connectors.EpayeApiConfig
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import unit.AppSpec
import unit.auth.AuthComponents.AuthOk

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.io.Source

class MonthlyStatementSpec extends AppSpec with BeforeAndAfterEach {
  val ton = EnrolmentIdentifier("TaxOfficeNumber", "921")
  val tor = EnrolmentIdentifier("TaxOfficeReference", "PE91702")

  val empRef = EmpRef(ton.value, tor.value)
  val taxYear = TaxYear(2017)
  val taxMonth = TaxMonth(taxYear, 3)

  implicit val hc = HeaderCarrier()
  val http = mock[WSHttp]
  val app =
    builder
      .update { _.overrides(bind(classOf[WSHttp]).toInstance(http)) }

  val activeEnrolment = AuthOk(Enrolment("IR-PAYE", Seq(ton, tor), "activated", Some("300")))
  val inactiveEnrolment = AuthOk(activeEnrolment.data.copy(state = "inactive"))
  val differentEnrolment = AuthOk(Enrolment("IR-Else", Seq(ton, tor), "activated", Some("300")))

  def config(implicit a: Application): EpayeApiConfig =
    inject[EpayeApiConfig]

  def request(implicit a: Application): Accumulator[ByteString, Result] =
    inject[GetMonthlyStatementController].getStatement(empRef, taxYear, taxMonth)(FakeRequest())

  override protected def beforeEach(): FixtureParam = {
    reset(http)
  }

  "The MonthlyStatement endpoint" should {
    "return 403 Forbidden on inactive enrolments" in new App(app.withAuth(inactiveEnrolment).build) {
      status(request) shouldBe FORBIDDEN
    }
    "return 403 Forbidden with different enrolments" in new App(app.withAuth(differentEnrolment).build) {
      status(request) shouldBe FORBIDDEN
    }
    "return 404 NotFound when the statements are not found" in new App(app.withAuth(activeEnrolment).build) {
      val epayeUrl =
        s"${config.epayeBaseUrl}" +
          s"/epaye/${empRef.encodedValue}" +
          s"/api/v1" +
          s"/monthly-statement/${taxYear.asString}/${taxMonth.asString}"
      Mockito.when(http.GET[HttpResponse](Matchers.eq(epayeUrl))(anyObject(), anyObject(), anyObject())).thenReturn {
        successful {
          HttpResponse(NOT_FOUND)
        }
      }

      status(request) shouldBe NOT_FOUND
    }
    "return 200 OK with the found statement" in new App(app.withAuth(activeEnrolment).build) {
      val inputJsonString: String = getResourceAsString("/epaye/monthly-statement/in/2017-3.json")
      val epayeUrl =
        s"${config.epayeBaseUrl}" +
          s"/epaye/${empRef.encodedValue}" +
          s"/api/v1" +
          s"/monthly-statement/${taxYear.asString}/${taxMonth.asString}"
      Mockito.when(http.GET[HttpResponse](Matchers.eq(epayeUrl))(anyObject(), anyObject(), anyObject())).thenReturn {
        successful {
          HttpResponse(OK, responseString = Some(inputJsonString))
        }
      }

      status(request) shouldBe OK
      val expectedJson =
        Json.parse(
          getResourceAsString("/epaye/monthly-statement/out/2017-3.json")
            .replaceAllLiterally("%{ton}", empRef.taxOfficeNumber)
            .replaceAllLiterally("%{tor}", empRef.taxOfficeReference)
            .replaceAllLiterally("%{apiBaseUrl}", apiBaseUrl)
        )
      contentAsJson(request) shouldBe expectedJson
    }
  }

  def contentAsPrettyJson(result: Future[play.api.mvc.Result]): String = {
    Json.prettyPrint(contentAsJson(result))
  }

  def prettyPrint(string: String): String =
    prettyPrint(Json.parse(string))

  def prettyPrint(json: JsValue): String =
    Json.prettyPrint(json)

  def getResourceAsString(name: String): String =
    Source.fromURL(getClass.getResource(name), "utf-8").mkString("")

  def getResourceAsJson(name: String): JsValue =
    Json.parse(getResourceAsString(name))
}
