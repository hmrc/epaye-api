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
import uk.gov.hmrc.epayeapi.models.TaxYear
import uk.gov.hmrc.epayeapi.models.out.ApiErrorJson
import uk.gov.hmrc.epayeapi.util.TestTimeMachine.withFixedLocalDate

class GetStatementsSpec extends IntegrationTestBase {

  "statements" should {
    val regYear = TaxYear(2014)
    val currentTaxYear = TaxYear(2016)

    "return 200 OK with annual statement links" in new Setup { withFixedLocalDate(currentTaxYear.firstDay) {
      given()
        .clientWith(empRef).isAuthorized
        .and()
        .epayeMasterDataReturns(
          s"""
            |{
            |  "accountsOfficeReference": "${empRef.taxOfficeReference}",
            |  "yearRegistered": {
            |    "yearFrom": ${regYear.yearFrom}
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
             |  "_embedded": {
             |    "statements": [{
             |      "taxYear": {
             |        "year": "2014-15",
             |        "firstDay": "2014-04-06",
             |        "lastDay": "2015-04-05"
             |      },
             |      "_links": {
             |        "self": {
             |          "href": "https://api.service.hmrc.gov.uk/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2014-15"
             |        }
             |      }
             |    }, {
             |      "taxYear": {
             |        "year": "2015-16",
             |        "firstDay": "2015-04-06",
             |        "lastDay": "2016-04-05"
             |      },
             |      "_links": {
             |        "self": {
             |          "href": "https://api.service.hmrc.gov.uk/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2015-16"
             |        }
             |      }
             |    }, {
             |      "taxYear": {
             |        "year": "2016-17",
             |        "firstDay": "2016-04-06",
             |        "lastDay": "2017-04-05"
             |      },
             |      "_links": {
             |        "self": {
             |          "href": "https://api.service.hmrc.gov.uk/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2016-17"
             |        }
             |      }
             |    }]
             |  },
             |  "_links": {
             |    "empRefs": {
             |      "href": "https://api.service.hmrc.gov.uk/organisations/paye/"
             |    },
             |    "self": {
             |      "href": "https://api.service.hmrc.gov.uk/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements"
             |    }
             |  }
             |}
        """.stripMargin
        ))
    }}

    "return 500 Internal Server Error if upstream returns invalid JSON" in new Setup {
      given()
        .clientWith(empRef).isAuthorized
        .and()
        .epayeMasterDataReturns("{not json}")
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
        .epayeMasterDataReturns("", 404)
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
          .epayeMasterDataReturns("", status)
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
    val apiBaseUrl = app.configuration.underlying.getString("api.baseUrl")
    val url = s"$baseUrl/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements"
  }
}
