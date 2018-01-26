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

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, EssentialAction, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.epayeapi.connectors.{EpayeApiConfig, EpayeConnector}
import uk.gov.hmrc.epayeapi.models.Formats._
import uk.gov.hmrc.epayeapi.models.in._
import uk.gov.hmrc.epayeapi.models.out.ApiErrorJson.EmpRefNotFound
import uk.gov.hmrc.epayeapi.models.out.{ApiErrorJson, EmpRefsJson}

import scala.concurrent.ExecutionContext

@Singleton
case class GetEmpRefsController @Inject() (
  config: EpayeApiConfig,
  authConnector: AuthConnector,
  epayeConnector: EpayeConnector,
  implicit val ec: ExecutionContext,
  implicit val mat: Materializer
)
  extends ApiController {

  def getEmpRefs(): EssentialAction = AuthorisedAction(epayeEnrolment) {
    Action.async { implicit request =>
      val empRefs = epayeConnector.getEmpRefs(hc)
      empRefs.map {
        successHandler orElse errorHandler
      }
    }
  }

  def successHandler: PartialFunction[EpayeResponse[EpayeEmpRefsResponse], Result] = {
    case EpayeSuccess(EpayeEmpRefsResponse(empRefs)) =>
      val empRefsJson = EmpRefsJson.fromSeq(config.apiBaseUrl, empRefs)
      Ok(Json.toJson(empRefsJson))
  }

  def errorHandler: PartialFunction[EpayeResponse[EpayeEmpRefsResponse], Result] = {
    case EpayeJsonError(err) =>
      Logger.error(s"Upstream returned invalid json: $err")
      InternalServerError(Json.toJson(ApiErrorJson.InternalServerError))
    case EpayeNotFound() =>
      NotFound(Json.toJson(EmpRefNotFound))
    case error: EpayeResponse[_] =>
      Logger.error(s"Error while fetching totals: $error")
      InternalServerError(Json.toJson(ApiErrorJson.InternalServerError))
  }
}
