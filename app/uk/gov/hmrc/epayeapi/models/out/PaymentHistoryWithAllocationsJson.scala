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
import play.api.Logger
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}
import uk.gov.hmrc.epayeapi.models.in.{EpayePaymentAllocation, EpayePaymentHistoryWithAllocations, EpayePaymentHistoryWithAllocationsPayment, EpayeRtiPaymentAllocation}

case class PaymentHistoryWithAllocationsJson (
  taxOfficeNumber: String,
  taxOfficeReference: String,
  taxYear: TaxYear,
  payments: Seq[PaymentWithAllocations],
  _links: PaymentHistoryWithAllocationsLinks
)

case class PaymentWithAllocations(
  paymentDate: LocalDate,
  method: Option[String],
  amount: BigDecimal,
  allocatedAmount: BigDecimal,
  allocations: Seq[PaymentAllocation]
)

case class PaymentAllocation(
  taxYear: TaxYear,
  taxMonth: TaxMonth,
  amount: BigDecimal,
  code: Option[String]
)

object PaymentAllocation{
  def transform(epayePaymentAllocation: EpayePaymentAllocation): PaymentAllocation = {
    PaymentAllocation(
      epayePaymentAllocation.taxYear,
      TaxMonth(epayePaymentAllocation.taxYear, epayePaymentAllocation.taxMonth.month),
      epayePaymentAllocation.amount,
      epayePaymentAllocation.code.map{_.name}
    )
  }
}

case class PaymentHistoryWithAllocationsLinks (
  empRefs: Link,
  summary: Link,
  statements: Link,
  payments: Link,
  self: Link,
  next: Link,
  previous: Link
)

object PaymentWithAllocations {
  import uk.gov.hmrc.epayeapi.models.ImplicitOrderings.localDateDescendingOrdering

  implicit val paymentDescendingOrdering = Ordering.by[PaymentWithAllocations, (LocalDate, BigDecimal)](payment => (payment.paymentDate, -payment.amount))

  private lazy val paymentMethods: Map[String, String] = Map(
    "TPS RECEIPTS BY DEBIT CARD" -> "Debit Card",
    "PAYMENTS MADE BY CHEQUE" -> "Cheque",
    "CHEQUE RECEIPTS" -> "Cheque",
    "BACS RECEIPTS" -> "BACS",
    "CHAPS" -> "CHAPS",
    "TPS RECEIPTS BY CREDIT CARD" -> "Credit Card",
    "NATIONAL DIRECT DEBIT RECEIPTS" -> "Direct Debit",
    "BILLPAY/OLPG/GIROBANK" -> "Online Payment",
    "BANK LODGEMENT PAYMENT" -> "Bank Lodgement",
    "BANK GIRO RECEIPTS" -> "Giro Receipts",
    "BANK GIRO IN CREDITS" -> "Giro Credits",
    "FPS RECEIPTS" -> "FPS Receipts",
    "CREDIT FOR INTERNET RECEIPTS" -> "Internet Receipts",
    "GIROBANK RECEIPTS" -> "Girobank",
    "GIROBANK/ POST OFFICE" -> "Post Office",
    "NIL DECLARATIONS" -> "Nil Declarations",
    "PAYMASTER" -> "Paymaster",
    "VOLUNTARY DIRECT PAYMENTS" -> "Voluntary Payments"
  ) withDefault { unknown =>
    Logger.warn(s"Invalid payment method: [${unknown}].")
    "UNKNOWN"
  }

  def transform(paymentMethodMaybe: Option[String]): Option[String] = {
    paymentMethodMaybe.map { paymentMethods }
  }
}

object PaymentHistoryWithAllocationsJson {
  def transform(
    apiBaseUrl: String,
    empRef: EmpRef,
    taxYear: TaxYear,
    epayePaymentHistory: EpayePaymentHistoryWithAllocations): PaymentHistoryWithAllocationsJson = {

    val payments: Seq[PaymentWithAllocations] = for {
      epayePayment <- epayePaymentHistory.payments
      epayePaymentDate <- epayePayment.paymentDate
    } yield toPaymentWithAllocations(epayePayment, epayePaymentDate)

    PaymentHistoryWithAllocationsJson(
      empRef.taxOfficeNumber,
      empRef.taxOfficeReference,
      taxYear,
      payments.sorted(PaymentWithAllocations.paymentDescendingOrdering),
      PaymentHistoryWithAllocationsLinks(
        empRefs = Link.empRefsLink,
        summary = Link.summaryLink(empRef),
        statements = Link.statementsLink(empRef),
        payments = Link.paymentHistoryLink(empRef, taxYear),
        self = Link.paymentHistoryWithAllocationsLink(empRef, taxYear),
        next = Link.paymentHistoryWithAllocationsLink(empRef, taxYear.next),
        previous = Link.paymentHistoryWithAllocationsLink(empRef, taxYear.previous)
      )
    )
  }

  private def toPaymentWithAllocations(
    epayePaymentWithAllocations: EpayePaymentHistoryWithAllocationsPayment,
    epayePaymentDate: LocalDate): PaymentWithAllocations = {

    PaymentWithAllocations(
      epayePaymentDate,
      PaymentWithAllocations.transform(epayePaymentWithAllocations.method),
      epayePaymentWithAllocations.amount,
      epayePaymentWithAllocations.allocatedAmount,
      epayePaymentWithAllocations.allocations.map{ PaymentAllocation.transform })
  }
}
