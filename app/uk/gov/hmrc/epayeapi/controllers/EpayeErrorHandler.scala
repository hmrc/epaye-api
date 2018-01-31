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

import play.api.Logger
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Controller, Result}
import uk.gov.hmrc.epayeapi.models.Formats._
import uk.gov.hmrc.epayeapi.models.in._
import uk.gov.hmrc.epayeapi.models.out.ApiErrorJson
import uk.gov.hmrc.epayeapi.models.out.ApiErrorJson.EmpRefNotFound

trait EpayeErrorHandler extends Controller {
  def errorHandler: PartialFunction[EpayeResponse[_], Result] = {
    case EpayeJsonError(err) =>
      Logger.error(s"epaye service returned invalid json: ${Json.prettyPrint(JsError.toJson(err))}")
      InternalServerError(Json.toJson(ApiErrorJson.InternalServerError))
    case EpayeNotFound() =>
      Logger.info("epaye service returned a 404")
      NotFound(Json.toJson(EmpRefNotFound))
    case EpayeError(status, _) =>
      Logger.error(s"epaye service returned unexpected response: status=$status")
      InternalServerError(Json.toJson(ApiErrorJson.InternalServerError))
    case EpayeException(message) =>
      Logger.error(s"Caught exception while calling epaye service: message=$message")
      InternalServerError(Json.toJson(ApiErrorJson.InternalServerError))
  }

}
