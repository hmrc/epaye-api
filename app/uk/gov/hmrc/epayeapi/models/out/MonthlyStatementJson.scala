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

package uk.gov.hmrc.epayeapi.models.out

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.in._
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}

case class MonthlyStatementJson(
  taxOfficeNumber: String,
  taxOfficeReference: String,
  taxYear: TaxYear,
  taxMonth: TaxMonth,
  rtiCharges: Seq[ChargeJson],
  interest: Seq[ChargeJson],
  allocatedCredits: Seq[ChargeJson],
  allocatedPayments: Seq[PaymentJson],
  writeOffs: Seq[ChargeJson],
dueDate: LocalDate,
  summary: MonthlySummaryJson,
  _links: MonthlyStatementLinksJson
)

case class ChargeJson(
  code: String,
  amount: BigDecimal
)

case class PaymentJson(
  paymentDate: LocalDate,
  amount: BigDecimal
)

case class MonthlySummaryJson(
  amount: BigDecimal,
  interest: BigDecimal,
  clearedByCredits: BigDecimal,
  clearedByPayments: BigDecimal,
  clearedByWriteOffs: BigDecimal,
  balance: BigDecimal
)

case class MonthlyStatementLinksJson(
  empRefs: Link,
  statements: Link,
  annualStatement: Link,
  self: Link,
  next: Link,
  previous: Link
)

object MonthlyStatementJson {
  def apply(apiBaseUrl: String, empRef: EmpRef, taxYear: TaxYear, taxMonth: TaxMonth, json: EpayeMonthlyStatement): MonthlyStatementJson =
    MonthlyStatementJson(
      taxOfficeNumber = empRef.taxOfficeNumber,
      taxOfficeReference = empRef.taxOfficeReference,
      taxYear = taxYear,
      taxMonth = taxMonth,
      rtiCharges = Charges(json.charges.fps) ++ Charges(json.charges.cis) ++ Charges(json.charges.eps),
      interest = if (json.charges.others == 0) Seq.empty else Seq(ChargeJson("INTEREST", json.charges.others)),
      allocatedCredits = Charges(json.credits.fps) ++ Charges(json.credits.cis) ++ Charges(json.credits.eps),
      allocatedPayments = Payments(json.payments),
      writeOffs = Charges(json.writeOffs),
      dueDate = json.balance.dueDate,
      summary = MonthlySummaryJson(json),
      _links = MonthlyStatementLinksJson(apiBaseUrl, empRef, taxYear, taxMonth)
    )
}

object Charges {
  def apply(details: EpayeMonthlyChargesDetails): Seq[ChargeJson] =
    details.items.map { ChargeJson.apply }
}

object ChargeJson {
  def apply(item: EpayeMonthlyStatementItem): ChargeJson =
    new ChargeJson(item.code.name, item.amount)
}

object Payments {
  def apply(payments: EpayeMonthlyPaymentDetails): Seq[PaymentJson] =
    payments.items.map { PaymentJson.apply }
}

object PaymentJson {
  def apply(item: EpayeMonthlyPaymentItem): PaymentJson =
    new PaymentJson(item.dateOfPayment, item.amount)
}

object MonthlySummaryJson {
  def apply(json: EpayeMonthlyStatement): MonthlySummaryJson =
    MonthlySummaryJson(
      amount = json.charges.total - json.charges.others,
      interest = json.charges.others,
      clearedByCredits = json.credits.total,
      clearedByPayments = json.payments.total,
      clearedByWriteOffs = json.writeOffs.total,
      balance = json.balance.total
    )
}

object MonthlyStatementLinksJson {
  def apply(apiBaseUrl: String, empRef: EmpRef, taxYear: TaxYear, taxMonth: TaxMonth): MonthlyStatementLinksJson =
    MonthlyStatementLinksJson(
      empRefs =
        Link.empRefsLink(apiBaseUrl),
      statements =
        Link.summaryLink(apiBaseUrl, empRef),
      annualStatement =
        Link.anualStatementLink(apiBaseUrl, empRef, taxYear),
      self =
        Link.monthlyStatementLink(apiBaseUrl, empRef, taxYear, taxMonth),
      next =
        Link.monthlyStatementLink(apiBaseUrl, empRef, if (taxMonth.isLast) taxYear.next else taxYear, taxMonth.next),
      previous =
        Link.monthlyStatementLink(apiBaseUrl, empRef, if (taxMonth.isFirst) taxYear.previous else taxYear, taxMonth.previous)
    )
}
