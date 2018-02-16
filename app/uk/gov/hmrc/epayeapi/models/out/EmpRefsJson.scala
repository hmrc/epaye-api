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
import uk.gov.hmrc.time.TaxYearResolver

case class EmpRefsJson(
  _embedded: EmbeddedEmpRefs,
  _links: EmpRefsLinks
)

case class EmbeddedEmpRefs(
  empRefs: Seq[EmpRefItem]
)

object EmpRefsJson {
  def fromSeq(seq: Seq[EmpRef]): EmpRefsJson =
    EmpRefsJson(EmbeddedEmpRefs(seq.map(EmpRefItem(_))), EmpRefsLinks())

  def apply(empRef: EmpRef): EmpRefsJson = fromSeq(Seq(empRef))
}

case class EmpRefItem(taxOfficeNumber: String, taxOfficeReference: String, _links: EmpRefLinks)

object EmpRefItem {
  def apply(empRef: EmpRef): EmpRefItem =
    EmpRefItem(empRef.taxOfficeNumber, empRef.taxOfficeReference, EmpRefLinks(empRef))
}

case class EmpRefsLinks(self: Link)

object EmpRefsLinks {
  def apply(): EmpRefsLinks = new EmpRefsLinks(self = Link.empRefsLink)
}

case class EmpRefLinks(
  self: Link,
  statements: Link,
  currentStatement: Link
)

object EmpRefLinks {
  def apply(empRef: EmpRef): EmpRefLinks =
    EmpRefLinks(
      self = Link.summaryLink(empRef),
      statements = Link.statementsLink(empRef),
      currentStatement = Link.anualStatementLink(empRef, TaxYear(TaxYearResolver.currentTaxYear))
    )
}

