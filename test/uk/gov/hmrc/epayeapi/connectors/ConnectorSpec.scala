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

import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.{JsError, JsPath, Json}
import uk.gov.hmrc.epayeapi.models.in._
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.{failed, successful}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

case class TestData(num: Int)
object TestData {
  implicit val testDataFormat = Json.format[TestData]
}

case class TestConnector(http: HttpGet, implicit val ec: ExecutionContext) extends ConnectorBase {
  val url = "http://localhost/testdata"
  def getData(implicit hc: HeaderCarrier): Future[EpayeResponse[TestData]] =
    get[TestData](url, hc)
}

class ConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  trait Setup {
    implicit val hc = HeaderCarrier()
    val http = mock[HttpGet]
    val connector = TestConnector(http, global)
    val url = connector.url
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(15.seconds, 15.milliseconds)

  "ConnectorBase" should {
    "return ApiSuccess in case everything goes right" in new Setup {
      when(connector.http.GET(url)).thenReturn {
        successful {
          HttpResponse(Status.OK, responseString = Some("""{"num": 1}"""))
        }
      }

      connector.getData.futureValue shouldEqual EpayeSuccess(TestData(1))
    }
    "return ApiJsonError in case the JSON contains errors" in new Setup {
      when(connector.http.GET(url))
        .thenReturn {
          successful {
            HttpResponse(Status.OK, responseString = Some("""{"other": 1}"""))
          }
        }

      connector.getData.futureValue shouldEqual EpayeJsonError(JsError(JsPath \ "num", "error.path.missing"))
    }

    "return ApiNotFound on 404s from upstream" in new Setup {
      when(connector.http.GET(url))
        .thenReturn {
          successful {
            HttpResponse(Status.NOT_FOUND)
          }
        }

      connector.getData.futureValue shouldEqual EpayeNotFound[TestData]()
    }

    "return ApiNotFound on 404 exceptions" in new Setup {
      when(connector.http.GET(url))
        .thenReturn {
          failed {
            new NotFoundException("Not found")
          }
        }

      connector.getData.futureValue shouldEqual EpayeNotFound[TestData]()
    }
    "return ApiError on unexpected responses with status < 500" in new Setup {
      when(connector.http.GET(url))
        .thenReturn {
          failed {
            new ForbiddenException("Forbidden")
          }
        }

      connector.getData.futureValue shouldEqual EpayeError[TestData](Status.FORBIDDEN, "Forbidden")
    }

    "return ApiError on unexpected status codes from upstream" in new Setup {
      when(connector.http.GET(url))
        .thenReturn {
          successful {
            HttpResponse(Status.BAD_REQUEST, responseString = Some("Bad Request"))
          }
        }

      connector.getData.futureValue shouldEqual EpayeError[TestData](Status.BAD_REQUEST, "Bad Request")
    }

    "return ApiException when throwing exceptions while requesting data" in new Setup {
      when(connector.http.GET(url))
        .thenReturn {
          failed {
            new RuntimeException("Error fetching data")
          }
        }

      connector.getData.futureValue shouldEqual EpayeException[TestData]("Error fetching data")
    }
  }
}
