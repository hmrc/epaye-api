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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.Matchers
import play.api.Logger
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.in.EpayeEmpRefsResponse
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.http.ws.WSHttpResponse
import uk.gov.hmrc.epayeapi.models.Formats._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}

trait RestAssertions {
  protected def given() = new Givens()
  protected def when()(implicit wsClient: WSClient) = new When(wsClient)
}

class Givens {
  def clientWith(empRef: EmpRef): ClientWithEmpRefGivens = new ClientWithEmpRefGivens(empRef)
  def client: ClientGivens = new ClientGivens

}

trait BaseClientGivens[A <: BaseClientGivens[A]] { self: A =>
  def isMissingBearerToken: A = isUnauthorizedWithError("MissingBearerToken")
  def isInsufficientConfidenceLevel: A = isUnauthorizedWithError("InsufficientConfidenceLevel")
  def isUnsupportedAffinityGroup: A = isUnauthorizedWithError("UnsupportedAffinityGroup")
  def isUnsupportedCredentialRole: A = isUnauthorizedWithError("UnsupportedCredentialRole")
  def isUnsupportedAuthProvider: A = isUnauthorizedWithError("UnsupportedAuthProvider")
  def isBearerTokenExpired: A = isUnauthorizedWithError("BearerTokenExpired")
  def isInvalidBearerToken: A = isUnauthorizedWithError("InvalidBearerToken")
  def isSessionRecordNotFound: A = isUnauthorizedWithError("SessionRecordNotFound")
  def isIncorrectCredentialStrength: A = isUnauthorizedWithError("IncorrectCredentialStrength")
  def isUnauthorizedWithError(error: String = "MissingBearerToken"): A = {
    stubFor {
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn {
          aResponse
            .withBody("{}")
            .withHeader(HeaderNames.WWW_AUTHENTICATE, s"""MDTP detail="$error"""")
            .withStatus(401)
        }
    }
    this
  }
  def when()(implicit wsClient: WSClient): When = new When(wsClient)
  def and(): A = this
}

class ClientGivens extends BaseClientGivens[ClientGivens] {

  def isAuthorized: ClientGivens = {
    stubFor {
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn {
          aResponse()
            .withBody(Fixtures.authorised)
            .withStatus(200)
        }
    }
    this
  }

  def epayeEmpRefsEndpointReturns(response: String): ClientGivens = {
    epayeEmpRefsEndpointReturns(200, response)
  }

  def epayeEmpRefsEndpointReturns(status: Int, response: String): ClientGivens = {
    stubFor {
      get(urlPathEqualTo(s"/epaye/self/api/v1/emprefs"))
        .willReturn {
          aResponse()
            .withBody(response)
            .withHeader("Content-Type", "application/json")
            .withStatus(status)
        }
    }
    this
  }
}

class ClientWithEmpRefGivens(empRef: EmpRef) extends BaseClientGivens[ClientWithEmpRefGivens] {
  def epayeTotalsReturns(body: String): ClientWithEmpRefGivens = {
    stubFor(
      get(
        urlPathEqualTo(s"/epaye/${empRef.encodedValue}/api/v1/annual-statement")
      ).willReturn(
          aResponse()
            .withBody(body)
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
        )
    )

    this
  }

  def epayeAnnualStatementReturns(body: String): ClientWithEmpRefGivens = {
    stubFor(
      get(
        urlPathEqualTo(s"/epaye/${empRef.encodedValue}/api/v1/annual-statement")
      ).willReturn(
          aResponse()
            .withBody(body)
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
        )
    )

    this
  }

  def epayeMonthlyStatementReturns(body: String): ClientWithEmpRefGivens = {
    stubFor(
      get(
        urlPathEqualTo(s"/epaye/${empRef.encodedValue}/api/v1/monthly-statement")
      ).willReturn(
          aResponse()
            .withBody(body)
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
        )
    )

    this
  }

  def isAuthorized: ClientWithEmpRefGivens = {
    stubFor(
      post(
        urlPathEqualTo(s"/auth/authorise")
      ).willReturn(
          aResponse()
            .withBody(Fixtures.authorisedEnrolmentJson(empRef))
            .withStatus(200)
        )
    )
    this
  }
}

class Assertions(response: HttpResponse) extends Matchers {
  def bodyIsOfJson(json: JsValue): Assertions = {
    Json.parse(response.body) shouldEqual json
    this
  }

  def bodyIsOfSchema(schemaPath: String): Unit = {
    val report = Schema(schemaPath).validate(response.body)

    withClue(report.toString) { report.isSuccess shouldBe true }
  }

  def statusCodeIs(statusCode: Int): Assertions = {
    response.status shouldBe statusCode
    this
  }

  def printBody(): Assertions = {
    println(s"Response body=${response.body}")
    this
  }

  def prettyPrintBody(): Assertions = {
    println(Json.prettyPrint(Json.parse(response.body)))
    this
  }

  def printStatus(): Assertions = {
    println(s"Response status=${response.status}")
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
    Logger.info(s"Requesting: ${url}")
    RequestExecutor(
      wsClient.url(url).withRequestTimeout(Duration(3, SECONDS))
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
