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

import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{ConfidenceLevel, Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.config.WSHttp
import uk.gov.hmrc.epayeapi.connectors.EpayeApiConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import unit.AppSpec
import unit.auth.AuthComponents.AuthOk

import scala.concurrent.Future
import scala.concurrent.Future.successful

class GetSummarySpec extends AppSpec with BeforeAndAfterEach {
  val ton = EnrolmentIdentifier("TaxOfficeNumber", "001")
  val tor = EnrolmentIdentifier("TaxOfficeReference", "AB00001")
  val empRef = EmpRef(ton.value, tor.value)

  implicit val hc = HeaderCarrier()
  val http = mock[WSHttp]

  val app = builder
    .update(_.overrides(bind(classOf[WSHttp]).toInstance(http)))

  val activeEnrolment =
    AuthOk(Enrolment("IR-PAYE", Seq(ton, tor), "activated", Some("300")))

  val inactiveEnrolment =
    AuthOk(activeEnrolment.data.copy(state = "inactive"))

  val differentEnrolment =
    AuthOk(Enrolment("IR-Else", Seq(ton, tor), "activated", Some("300")))

  def config(implicit a: Application): EpayeApiConfig =
    inject[EpayeApiConfig]

  def request(implicit a: Application): Future[Result] =
    inject[GetSummaryController].getSummary(empRef)(FakeRequest())


  override protected def beforeEach(): FixtureParam = {
    reset(http)
  }

  "The summary endpoint" should {
    "return 200 OK on active enrolments" in new App(app.withAuth(activeEnrolment).build) {
      val firstUrl =  s"${config.epayeBaseUrl}" +
                      s"/epaye" +
                      s"/${empRef.encodedValue}" +
                      s"/api/v1/annual-statement"

      when(http.GET[HttpResponse](Matchers.eq(firstUrl))(anyObject(), anyObject(), anyObject())).thenReturn {
        successful {
          HttpResponse(OK, responseString = Some(
            """
              |{
              |  "rti": {
              |    "totals": {
              |      "balance": 100
              |    }
              |  },
              |  "nonRti": {
              |    "totals": {
              |      "balance": 23
              |    }
              |  }
              |}
            """.stripMargin
          ))
        }
      }

      contentAsString(request) shouldBe Json.parse(
        s"""
          |{
          |  "outstandingCharges": {
          |    "amount": 123,
          |    "breakdown": {
          |      "rti": 100,
          |      "nonRti": 23
          |    }
          |  },
          |  "_links" : {
          |    "empRefs": {
          |      "href": "$apiBaseUrl/organisations/paye/"
          |    },
          |    "self": {
          |      "href": "$apiBaseUrl/organisations/paye/001/AB00001"
          |    }
          |  }
          |}
        """.stripMargin).toString()
      status(request) shouldBe OK
    }

    "return 403 Forbidden on inactive enrolments" in new App(app.withAuth(inactiveEnrolment).build) {
      status(request) shouldBe FORBIDDEN
    }
    "return 403 Forbidden with different enrolments" in new App(app.withAuth(differentEnrolment).build) {
      status(request) shouldBe FORBIDDEN
    }
  }


}
