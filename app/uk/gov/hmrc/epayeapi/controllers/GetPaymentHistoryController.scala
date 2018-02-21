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
import play.api.libs.json.Json
import play.api.mvc.{Action, EssentialAction, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.connectors.{EpayeApiConfig, EpayeConnector}
import uk.gov.hmrc.epayeapi.models.Formats.paymentHistoryJsonWrites
import uk.gov.hmrc.epayeapi.models.TaxYear
import uk.gov.hmrc.epayeapi.models.in.{EpayePaymentHistory, EpayeResponse, EpayeSuccess}
import uk.gov.hmrc.epayeapi.models.out.PaymentHistoryJson

import scala.concurrent.ExecutionContext

@Singleton
case class GetPaymentHistoryController @Inject() (
  config: EpayeApiConfig,
  authConnector: AuthConnector,
  epayeConnector: EpayeConnector,
  implicit val ec: ExecutionContext,
  implicit val mat: Materializer
)
  extends ApiController
    with EpayeErrorHandler {

  def getPaymentHistory(empRef: EmpRef, taxYear: TaxYear): EssentialAction = {
    EmpRefAction(empRef) {
      Action.async { implicit request =>
        val epayePaymentHistory = epayeConnector.getPaymentHistory(empRef, taxYear, hc)
        epayePaymentHistory.map {
          successHandler(empRef, taxYear) orElse errorHandler
        }
      }
    }
  }

  private def successHandler(empRef: EmpRef, taxYear: TaxYear): PartialFunction[EpayeResponse[EpayePaymentHistory], Result] = {
    case EpayeSuccess(epayePaymentHistory) =>
      val paymentHistory = PaymentHistoryJson.transform(config.apiBaseUrl, empRef, taxYear, epayePaymentHistory)

      Ok(Json.toJson(paymentHistory))
  }
}
