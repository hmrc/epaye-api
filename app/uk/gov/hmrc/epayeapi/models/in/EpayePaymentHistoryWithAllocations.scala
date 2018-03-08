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

package uk.gov.hmrc.epayeapi.models.in

import org.joda.time.LocalDate
import uk.gov.hmrc.epayeapi.models.TaxYear

case class EpayePaymentHistoryWithAllocations(
  taxYear: TaxYear,
  payments: Seq[EpayePaymentHistoryWithAllocationsPayment]
)

case class EpayePaymentHistoryWithAllocationsPayment(
  paymentDate: Option[LocalDate],
  method: Option[String],
  amount: BigDecimal,
  allocatedAmount: BigDecimal,
  allocations: Seq[EpayePaymentAllocation]
)

trait EpayePaymentAllocation {
  def period: EpayeTaxPeriod
  def amount: BigDecimal
  def code: Option[EpayeCode]

  lazy val taxYear: TaxYear = TaxYear(period.startingTaxYear)
  lazy val taxMonth: EpayeTaxMonth = EpayeTaxMonth.fromLocalDate(period.taxFrom)
}

case class EpayeRtiPaymentAllocation(
  period: EpayeTaxPeriod,
  amount: BigDecimal
) extends EpayePaymentAllocation {
  lazy val code: Option[EpayeCode] = None
}

case class EpayeNonRtiPaymentAllocation(
  period: EpayeTaxPeriod,
  amount: BigDecimal,
  code: Option[EpayeCode]
) extends EpayePaymentAllocation