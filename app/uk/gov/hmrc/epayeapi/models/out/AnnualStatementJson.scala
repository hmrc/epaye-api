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
import uk.gov.hmrc.epayeapi.models.in.{EpayeAnnualStatement, LineItem}
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}

case class PeriodJson(firstDay: LocalDate, lastDay: LocalDate)

case class NonRtiChargesJson(
  code: String,
  amount: BigDecimal,
  clearedByCredits: BigDecimal,
  clearedByPayments: BigDecimal,
  clearedByWriteOffs: BigDecimal,
  balance: BigDecimal,
  dueDate: LocalDate
)

object NonRtiChargesJson {
  def from(lineItem: LineItem, taxYear: TaxYear): Option[NonRtiChargesJson] = {
    for {
      code <- lineItem.codeText
    } yield NonRtiChargesJson(
      code = code,
      amount = lineItem.charges,
      clearedByCredits = lineItem.credits,
      clearedByPayments = lineItem.payments,
      clearedByWriteOffs = lineItem.writeOffs,
      balance = lineItem.balance,
      dueDate = lineItem.dueDate
    )
  }
}

case class EarlierYearUpdateJson(
  amount: BigDecimal,
  clearedByCredits: BigDecimal,
  clearedByPayments: BigDecimal,
  clearedByWriteOffs: BigDecimal,
  balance: BigDecimal,
  dueDate: LocalDate
)

object EarlierYearUpdateJson {
  def extractFrom(lineItems: Seq[LineItem]): Option[EarlierYearUpdateJson] = {
    lineItems
      .find(_.itemType.contains("eyu"))
      .map { lineItem =>
        EarlierYearUpdateJson(
          amount = lineItem.charges,
          clearedByCredits = lineItem.credits,
          clearedByPayments = lineItem.payments,
          clearedByWriteOffs = lineItem.writeOffs,
          balance = lineItem.balance,
          dueDate = lineItem.dueDate
        )
      }
  }
}

case class EmbeddedRtiChargesJson(
  earlierYearUpdate: Option[EarlierYearUpdateJson],
  rtiCharges: Seq[MonthlyChargesJson]
)

case class MonthlyChargesJson(
  taxMonth: TaxMonth,
  amount: BigDecimal,
  clearedByCredits: BigDecimal,
  clearedByPayments: BigDecimal,
  clearedByWriteOffs: BigDecimal,
  balance: BigDecimal,
  dueDate: LocalDate,
  isSpecified: Boolean,
  _links: SelfLink
)

object MonthlyChargesJson {

  def from(lineItem: LineItem, empRef: EmpRef, taxYear: TaxYear): Option[MonthlyChargesJson] = {
    for {
      epayeTaxMonth <- lineItem.taxMonth
      taxMonth = TaxMonth(taxYear, epayeTaxMonth.month)
    } yield MonthlyChargesJson(
      taxMonth = taxMonth,
      amount = lineItem.charges,
      clearedByCredits = lineItem.credits,
      clearedByPayments = lineItem.payments,
      clearedByWriteOffs = lineItem.writeOffs,
      balance = lineItem.balance,
      dueDate = lineItem.dueDate,
      isSpecified = lineItem.isSpecified,
      _links = SelfLink(Link.monthlyStatementLink(empRef, taxYear, taxMonth))
    )
  }
}

case class SelfLink(
  self: Link
)

case class AnnualStatementLinksJson(
  empRefs: Link,
  summary: Link,
  statements: Link,
  self: Link,
  next: Link,
  previous: Link
)

case class AnnualStatementJson(
  taxOfficeNumber: String,
  taxOfficeReference: String,
  taxYear: TaxYear,
  nonRtiCharges: Seq[NonRtiChargesJson],
  _embedded: EmbeddedRtiChargesJson,
  _links: AnnualStatementLinksJson
)

object AnnualStatementJson {
  def apply(apiBaseUrl: String, empRef: EmpRef, taxYear: TaxYear, epayeAnnualStatement: EpayeAnnualStatement): AnnualStatementJson =
    AnnualStatementJson(
      taxOfficeNumber = empRef.taxOfficeNumber,
      taxOfficeReference = empRef.taxOfficeReference,
      taxYear = taxYear,
      _embedded = EmbeddedRtiChargesJson(
        EarlierYearUpdateJson.extractFrom(epayeAnnualStatement.rti.lineItems),
        epayeAnnualStatement.rti.lineItems.flatMap(MonthlyChargesJson.from(_, empRef, taxYear))
      ),
      nonRtiCharges = epayeAnnualStatement.nonRti.lineItems.flatMap(NonRtiChargesJson.from(_, taxYear)),
      _links = AnnualStatementLinksJson(
        empRefs = Link.empRefsLink,
        summary = Link.summaryLink(empRef),
        statements = Link.statementsLink(empRef),
        self = Link.annualStatementLink(empRef, taxYear),
        next = Link.annualStatementLink(empRef, taxYear.next),
        previous = Link.annualStatementLink(empRef, taxYear.previous)
      )
    )

}
