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

package contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.Matchers
import play.api.libs.ws.{WSClient, WSRequest}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.http.ws.WSHttpResponse

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}

trait RestAssertions {
  protected def given() = new Givens()
}

class Givens {
  def clientWith(empRef: EmpRef): ClientGivens = new ClientGivens(empRef)
}

class ClientGivens(empRef: EmpRef) {

  def when()(implicit wsClient: WSClient): When = new When(wsClient)

  def and(): ClientGivens = this

  def epayeTotalsReturns(owed: BigDecimal): ClientGivens = {
    val response = aResponse()

    val body =
      """
        |{
        |  "rti": {
        |     "totals": {
        |       "balance": {
        |         "credit": 10,
        |         "debit": 20
        |       }
        |     }
        |  },
        |  "nonRti": {
        |     "totals": {
        |       "balance": {
        |         "credit": 10,
        |         "debit": 20
        |       }
        |     }
        |  }
        |}
      """.stripMargin

    response
      .withBody(body)
      .withHeader("Content-Type", "application/json")
      .withStatus(200)

    stubFor(
      get(urlPathEqualTo(s"/epaye/${empRef.encodedValue}/api/v1/annual-statement")).willReturn(response)
    )

    this
  }

  def isAuthorized: ClientGivens = {

    val responseBody =
      s"""
        |{
        |  "authorisedEnrolments": [
        |    {
        |      "key": "IR-PAYE",
        |      "identifiers": [
        |        {
        |          "key": "TaxOfficeNumber",
        |          "value": "${empRef.taxOfficeNumber}"
        |        },
        |        {
        |          "key": "TaxOfficeReference",
        |          "value": "${empRef.taxOfficeReference}"
        |        }],
        |      "state": "Activated",
        |      "confidenceLevel": 0,
        |      "delegatedAuthRule": "epaye-auth",
        |      "enrolment": "IR-PAYE"
        |    }
        |  ]
        |}
      """.stripMargin

    val response = aResponse().withBody(responseBody).withStatus(200)

    stubFor(
      post(urlPathEqualTo(s"/auth/authorise")).willReturn(response)
    )
    this
  }

}

class Assertions(response: HttpResponse) extends Matchers {

  def bodyIsOfSchema(schemaPath: String): Unit = {
    val validator = JsonSchemaFactory.byDefault().getJsonSchema(schemaPath)

    validator.validate(new ObjectMapper().readTree(response.body), true)
  }

  def statusCodeIs(statusCode: Int): Assertions = {
    response.status shouldBe statusCode
    this
  }

}

case class RequestExecutor(request: WSRequest) {

  def withAuthHeader(): RequestExecutor = {
    RequestExecutor(request.withHeaders(("Authorization", "foobar")))
  }

  def thenAssertThat(): Assertions = new Assertions(Http.execute(request))
}

class When(wsClient: WSClient) {
  def get(url: String): RequestExecutor = {
    RequestExecutor(
      wsClient.url(url).withRequestTimeout(Duration(2, SECONDS))
    )
  }
}

object Http {
  def execute(request: WSRequest): WSHttpResponse =
    Await.result(
      request.get().map(new WSHttpResponse(_)),
      Duration(10, SECONDS)
    )
}