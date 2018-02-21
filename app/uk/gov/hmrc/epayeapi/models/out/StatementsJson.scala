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
import uk.gov.hmrc.epayeapi.models.TaxYear

case class StatementsJson(
  taxOfficeNumber: String,
  taxOfficeReference: String,
  _embedded: Embedded,
  _links: Links
)

case class Embedded(
  statements: Seq[Statement]
)

case class Statement(
  taxYear: TaxYear,
  _links: StatementLinks
)

case class StatementLinks(
  self: Link
)

case class Links(
  empRefs: Link,
  self: Link
)

object StatementsJson {
  def apply(empRef: EmpRef, taxYearOfRegistration: Option[TaxYear]): StatementsJson = {
    def taxYearStatement(taxYear: TaxYear): Statement = {
      Statement(
        taxYear = taxYear,
        _links = StatementLinks(self = Link.anualStatementLink(empRef, taxYear))
      )
    }

    val currentTaxYear = TaxYear(LocalDate.now())

    val statements: Seq[Statement] = for {
      regYear <- taxYearOfRegistration.toSeq
      taxYearFrom <- (regYear.yearFrom to currentTaxYear.yearFrom)
      taxYear = TaxYear(taxYearFrom)
    } yield taxYearStatement(taxYear)

    val links = Links(
      empRefs = Link.empRefsLink,
      self = Link.statementsLink(empRef)
    )

    StatementsJson(
      taxOfficeNumber = empRef.taxOfficeNumber,
      taxOfficeReference = empRef.taxOfficeReference,
      _embedded = Embedded(statements),
      _links = links
    )
  }
}