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

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, Reads}
import uk.gov.hmrc.epayeapi.models.in._
import uk.gov.hmrc.epayeapi.syntax.json._
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

trait ConnectorBase {
  def http: HttpGet
  implicit def ec: ExecutionContext

  private[connectors] def get[A](url: String, headers: HeaderCarrier)(implicit jsonReader: Reads[A]): Future[EpayeResponse[A]] = {
    Logger.debug(s"ApiClient GET request: url=$url headers=$headers")

    val result = for {
      response <- http.GET[HttpResponse](url)(HttpReads.readRaw, headers, ec)
    } yield reader(jsonReader).read("GET", url, response)

    result.recover({
      case ex: HttpException =>
        ex.responseCode match {
          case Status.NOT_FOUND =>
            EpayeNotFound()
          case statusCode: Int if statusCode < 500 =>
            EpayeError(statusCode, ex.message)
          case statusCode: Int =>
            Logger.error(s"Upstream returned unexpected response: status=$statusCode", ex)
            EpayeException(ex.getMessage)
        }

      case ex: Exception =>
        Logger.error("HTTP request threw exception", ex)
        EpayeException(ex.getMessage)
    })
  }

  private def reader[A](jsonReader: Reads[A]): HttpReads[EpayeResponse[A]] =
    new HttpReads[EpayeResponse[A]] {
      def read(method: String, url: String, response: HttpResponse): EpayeResponse[A] = {
        response.status match {
          case Status.OK =>
            response.body.parseAndValidate[A](jsonReader) match {
              case JsSuccess(obj, _) => EpayeSuccess(obj)
              case err: JsError => EpayeJsonError(err)
            }
          case Status.NOT_FOUND =>
            EpayeNotFound()
          case statusCode: Int if statusCode < 500 =>
            EpayeError(response.status, response.body)
          case statusCode: Int =>
            EpayeException(response.body)
        }
      }
    }
}

