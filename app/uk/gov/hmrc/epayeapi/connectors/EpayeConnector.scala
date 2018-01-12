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

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.config.WSHttp
import uk.gov.hmrc.epayeapi.models.Formats._
import uk.gov.hmrc.epayeapi.models.in._
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class EpayeApiConfig(
  epayeBaseUrl: String,
  apiBaseUrl: String
)

@Singleton
case class EpayeConnector @Inject() (
  config: EpayeApiConfig,
  http: WSHttp,
  implicit val ec: ExecutionContext
) extends ConnectorBase {

  def getTotal(empRef: EmpRef, headers: HeaderCarrier): Future[EpayeResponse[EpayeTotalsResponse]] = {
    val url =
      s"${config.epayeBaseUrl}" +
        s"/epaye" +
        s"/${empRef.encodedValue}" +
        s"/api/v1/annual-statement"

    get[EpayeTotalsResponse](url, headers)
  }

  def getAnnualStatement(empRef: EmpRef, taxYear: TaxYear, headers: HeaderCarrier): Future[EpayeResponse[EpayeAnnualStatement]] = {
    val url =
      s"${config.epayeBaseUrl}" +
        s"/epaye" +
        s"/${empRef.encodedValue}" +
        s"/api/v1/annual-statement/${taxYear.asString}"

    get[EpayeAnnualStatement](url, headers)
  }

  def getMonthlyStatement(empRef: EmpRef, headers: HeaderCarrier, taxYear: TaxYear, taxMonth: TaxMonth): Future[EpayeResponse[EpayeMonthlyStatement]] = {
    val url =
      s"${config.epayeBaseUrl}" +
        s"/epaye/${empRef.encodedValue}" +
        s"/api/v1" +
        s"/monthly-statement/${taxYear.asString}/${taxMonth.asString}"

    get[EpayeMonthlyStatement](url, headers)
  }
}

