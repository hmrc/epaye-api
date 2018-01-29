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

package uk.gov.hmrc.epayeapi.connectors

import common.EmpRefGenerator
import org.joda.time.LocalDate
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.domain.EmpRef

import uk.gov.hmrc.epayeapi.config.WSHttp
import uk.gov.hmrc.epayeapi.models.in._
import uk.gov.hmrc.epayeapi.models.Formats._
import uk.gov.hmrc.epayeapi.models.{JsonFixtures, TaxYear}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.{successful, failed}
import scala.concurrent.duration._


class EpayeConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {

  trait Setup {
    implicit val hc = HeaderCarrier()
    val http = mock[WSHttp]
    val config = EpayeApiConfig("https://EPAYE", "http://localhost")
    val connector = EpayeConnector(config, http, global)
    val empRef = EmpRefGenerator.getEmpRef
    val urlEmpRefs = s"${config.epayeBaseUrl}/epaye/self/api/v1/emprefs"
    val urlTotals = s"${config.epayeBaseUrl}/epaye/${empRef.encodedValue}/api/v1/annual-statement"
    val urlTotalsByType = s"${config.epayeBaseUrl}/epaye/${empRef.encodedValue}/api/v1/totals/by-type"
    def urlAnnualStatement(taxYear: TaxYear): String =
      s"${config.epayeBaseUrl}/epaye/${empRef.encodedValue}/api/v1/annual-statement/${taxYear.asString}"

    def getRandomEmpRefs(num: Int): Seq[EmpRef] =
      for (_ <- 0 to num) yield EmpRefGenerator.getEmpRef

    def getEmpRefsResponse(empRefs: Seq[EmpRef]): String =
      Json.prettyPrint(Json.toJson(EpayeEmpRefsResponse(empRefs)))
  }

  "EpayeConnector.getEmpRefs" should {
    "return all empRefs a customer has access to" in new Setup {
      val empRefs = getRandomEmpRefs(10)
      val response = getEmpRefsResponse(empRefs)
      when(connector.http.GET(urlEmpRefs)).thenReturn {
        successful {
          HttpResponse(Status.OK, responseString = Some(response))
        }
      }

      await(connector.getEmpRefs(hc)) shouldBe EpayeSuccess(EpayeEmpRefsResponse(empRefs))
    }

    "return an exception when upstream returns a 502 Bad Gateway" in new Setup {
      val error = """{"error": "Error retrieving EmpRefs"}"""
      when(connector.http.GET(urlEmpRefs)).thenReturn {
        successful {
          HttpResponse(Status.BAD_GATEWAY, responseString = Some(error))
        }
      }

      await(connector.getEmpRefs(hc)) shouldBe EpayeException(error)
    }

    "return an exception when the client throws an exception" in new Setup {
      val error = """{"error": "Error retrieving EmpRefs"}"""

      when(connector.http.GET(urlEmpRefs)).thenReturn {
        failed {
          new BadGatewayException(error)
        }
      }

      await(connector.getEmpRefs(hc)) shouldBe EpayeException(error)
    }
  }

  "EpayeConnector" should {
    "retrieve the total credit and debit for a given empRef" in new Setup {
      when(connector.http.GET(urlTotals)).thenReturn {
        successful {
          HttpResponse(Status.OK, responseString = Some(
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

      Await.result(connector.getTotal(empRef, hc), 2.seconds) shouldBe
        EpayeSuccess(
          EpayeTotalsResponse(
            EpayeTotalsItem(EpayeTotals(100)),
            EpayeTotalsItem(EpayeTotals(23))
          )
        )
    }

    "retrieve summary for a given empRef" in new Setup {
      val taxYear = TaxYear(2016)

      when(connector.http.GET(urlAnnualStatement(taxYear))).thenReturn {
        successful {
          HttpResponse(Status.OK, responseString = Some(JsonFixtures.annualStatements.annualStatement))
        }
      }

      connector.getAnnualStatement(empRef, taxYear, hc).futureValue shouldBe
        EpayeSuccess(
          EpayeAnnualStatement(
            rti = AnnualStatementTable(
              List(
                LineItem(
                  taxYear = TaxYear(2017),
                  taxMonth = Some(EpayeTaxMonth(1)),
                  charges = 100.2,
                  payments = 0,
                  credits = 0,
                  writeOffs = 0,
                  balance = 100.2,
                  dueDate = new LocalDate(2017, 5, 22),
                  isSpecified = false,
                  codeText = None,
                  itemType = None
                )
              ),
              AnnualTotal(
                charges = 100.2,
                payments = 0,
                credits = 0,
                writeOffs = 0,
                balance = 100.2
              )
            ),
            nonRti = AnnualStatementTable(
              List(
                LineItem(
                  taxYear = TaxYear(2017),
                  taxMonth = None,
                  charges = 20.0,
                  payments = 0,
                  credits = 0,
                  writeOffs = 0,
                  balance = 20.0,
                  dueDate = new LocalDate(2018, 2, 22),
                  isSpecified = false,
                  codeText = Some("P11D_CLASS_1A_CHARGE"),
                  itemType = None
                )
              ),
              AnnualTotal(
                charges = 20.0,
                payments = 0,
                credits = 0,
                writeOffs = 0,
                balance = 20.0
              )
            ),
            unallocated = None
          )
        )
    }
  }
}
