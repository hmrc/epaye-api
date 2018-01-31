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

import play.api.libs.json.Json
import uk.gov.hmrc.epayeapi.models.Formats._
import uk.gov.hmrc.epayeapi.models.out.ApiErrorJson

class GetSummarySpec
  extends IntegrationTestBase {

  "The summary endpoint" should {
    "return 200 OK on active enrolments" in new Setup {
      given()
        .clientWith(empRef).isAuthorized
        .and()
        .epayeAnnualStatementReturns(
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
        )
        .when()
        .get(url)
        .thenAssertThat()
        .statusCodeIs(200)
        .bodyIsOfJson(Json.parse(
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
             |      "href": "$apiBaseUrl/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}"
             |    }
             |  }
             |}
        """.stripMargin
        ))

    }

    "return 500 Internal Server Error if upstream returns invalid JSON" in new Setup {
      given()
        .clientWith(empRef).isAuthorized
        .and()
        .epayeAnnualStatementReturns("{not json}")
        .when()
        .get(url)
        .withAuthHeader()
        .thenAssertThat()
        .statusCodeIs(500)
        .bodyIsOfJson(Json.toJson(ApiErrorJson.InternalServerError))
    }

    "return 404 Not Found if upstream returns a 404" in new Setup {
      given()
        .clientWith(empRef).isAuthorized
        .and()
        .epayeAnnualStatementReturns(404, "")
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
          .clientWith(empRef).isAuthorized
          .and()
          .epayeAnnualStatementReturns(status, "")
          .when()
          .get(url)
          .withAuthHeader()
          .thenAssertThat()
          .statusCodeIs(500)
          .bodyIsOfJson(Json.toJson(ApiErrorJson.InternalServerError))
      }
    }
  }

  it should new Setup {
    haveAuthentication(url)
  }

  trait Setup {
    val empRef = getEmpRef
    val url = s"$baseUrl/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}"
    val apiBaseUrl = app.configuration.underlying.getString("api.baseUrl")
  }
}
