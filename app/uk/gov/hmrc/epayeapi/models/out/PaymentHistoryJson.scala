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

import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.TaxYear
import uk.gov.hmrc.epayeapi.models.in.EpayePaymentHistory

case class PaymentHistoryJson (
  taxOfficeNumber: String,
  taxOfficeReference: String,
  taxYear: TaxYear,
  payments: Seq[Payment],
  _links: PaymentHistoryLinks
)

case class PaymentHistoryLinks (
  empRefs: Link,
  summary: Link,
  statements: Link,
  self: Link,
  next: Link,
  previous: Link
)

object PaymentHistoryJson {
  def transform(
    apiBaseUrl: String,
    empRef: EmpRef,
    taxYear: TaxYear,
    epayePaymentHistory: EpayePaymentHistory): PaymentHistoryJson = {

    val payments: Seq[Payment] = for {
      epayePayment <- epayePaymentHistory.payments
      epayePaymentDate <- epayePayment.dateOfPayment
    } yield Payment(epayePaymentDate, epayePayment.amount)

    PaymentHistoryJson(
      empRef.taxOfficeNumber,
      empRef.taxOfficeReference,
      taxYear,
      payments.sorted(Payment.paymentDescendingOrdering),
      PaymentHistoryLinks(
        empRefs = Link.empRefsLink,
        summary = Link.summaryLink(empRef),
        statements = Link.statementsLink(empRef),
        self = Link.paymentHistoryLink(empRef, taxYear),
        next = Link.paymentHistoryLink(empRef, taxYear.next),
        previous = Link.paymentHistoryLink(empRef, taxYear.previous)
      )
    )
  }
}