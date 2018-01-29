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
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.routing.Router
import uk.gov.hmrc.epayeapi.models.in.EpayeEmpRefsResponse
import uk.gov.hmrc.epayeapi.models.out.{ApiErrorJson, EmpRefsJson}
import uk.gov.hmrc.epayeapi.router.RoutesProvider
import uk.gov.hmrc.epayeapi.models.Formats._

class GetEmpRefsSpec
  extends WordSpec
  with Matchers
  with WSClientSetup
  with WiremockSetup
  with EmpRefGenerator
  with RestAssertions {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder().overrides(bind[Router].toProvider[RoutesProvider]).build()

  trait Setup {
    val url = baseUrl
    val apiBaseUrl = app.configuration.underlying.getString("api.baseUrl")
    val empRefs = for (_ <- 1 to 5) yield getEmpRef
    lazy val empRefJson = Json.toJson(EpayeEmpRefsResponse(empRefs)).toString()
  }

  "The EmpRefs API" should {
    "return 200 OK with a list of empRefs" in new Setup {
      given()
        .client.isAuthorized
        .and()
        .epayeEmpRefsEndpointReturns(empRefJson)
        .when()
        .get(url)
        .withAuthHeader()
        .thenAssertThat()
        .statusCodeIs(200)
        .bodyIsOfJson(Json.toJson(EmpRefsJson.fromSeq(apiBaseUrl, empRefs)))
    }

    "return 500 Internal Server error if upstream returns invalid JSON" in new Setup {
      given()
        .client.isAuthorized
        .and()
        .epayeEmpRefsEndpointReturns("""{not json}""")
        .when()
        .get(url)
        .withAuthHeader()
        .thenAssertThat()
        .statusCodeIs(500)
        .bodyIsOfJson(Json.toJson(ApiErrorJson.InternalServerError))
    }

    "return 404 Not Found if upstream returns a 404" in new Setup {
      given()
        .client.isAuthorized
        .and()
        .epayeEmpRefsEndpointReturns(404, "")
        .when()
        .get(url)
        .withAuthHeader()
        .thenAssertThat()
        .statusCodeIs(404)
        .bodyIsOfJson(Json.toJson(ApiErrorJson.EmpRefNotFound))
    }

    "return a 500 Internal Server Error on errors from upstream" in new Setup {
      for (status <- Seq(400, 401, 402, 403, 502, 503)) {
        given()
          .client.isAuthorized
          .and()
          .epayeEmpRefsEndpointReturns(status, "")
          .when()
          .get(url)
          .withAuthHeader()
          .thenAssertThat()
          .statusCodeIs(500)
          .bodyIsOfJson(Json.toJson(ApiErrorJson.InternalServerError))
      }
    }

    "return 401 Unauthorized with invalid bearer token" in new Setup {
      given()
        .client.isInvalidBearerToken
        .and()
        .epayeEmpRefsEndpointReturns(empRefJson)
        .when()
        .get(url)
        .withAuthHeader()
        .thenAssertThat()
        .statusCodeIs(401)
        .bodyIsOfJson(Json.toJson(ApiErrorJson.InvalidBearerToken))
    }

    "return 401 Unauthorized with missing bearer token" in new Setup {
      given()
        .client.isMissingBearerToken
        .and()
        .epayeEmpRefsEndpointReturns(empRefJson)
        .when()
        .get(url)
        .withAuthHeader()
        .thenAssertThat()
        .statusCodeIs(401)
        .bodyIsOfJson(Json.toJson(ApiErrorJson.MissingBearerToken))
    }

    "return 401 Unauthorized with an expired bearer token" in new Setup {
      given()
        .client.isBearerTokenExpired
        .and()
        .epayeEmpRefsEndpointReturns(empRefJson)
        .when()
        .get(url)
        .withAuthHeader()
        .thenAssertThat()
        .statusCodeIs(401)
        .bodyIsOfJson(Json.toJson(ApiErrorJson.ExpiredBearerToken))
    }

    "return 401 Unauthorized with other auth errors" in new Setup {
      given()
        .client.isUnsupportedAuthProvider
        .and()
        .epayeEmpRefsEndpointReturns(empRefJson)
        .when()
        .get(url)
        .withAuthHeader()
        .thenAssertThat()
        .statusCodeIs(401)
        .bodyIsOfJson(Json.toJson(ApiErrorJson.AuthorisationError))
    }
  }
}
