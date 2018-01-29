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
  def fromSeq(apiBaseUrl: String, seq: Seq[EmpRef]): EmpRefsJson =
    EmpRefsJson(EmbeddedEmpRefs(seq.map(EmpRefItem(apiBaseUrl, _))), EmpRefsLinks(apiBaseUrl))

  def apply(apiBaseUrl: String, empRef: EmpRef): EmpRefsJson =
    fromSeq(apiBaseUrl, Seq(empRef))
}

case class EmpRefItem(taxOfficeNumber: String, taxOfficeReference: String, _links: EmpRefLinks)

object EmpRefItem {
  def apply(apiBaseUrl: String, empRef: EmpRef): EmpRefItem =
    EmpRefItem(empRef.taxOfficeNumber, empRef.taxOfficeReference, EmpRefLinks(apiBaseUrl, empRef))
}

case class EmpRefsLinks(self: Link)

object EmpRefsLinks {
  def apply(apiBaseUrl: String): EmpRefsLinks =
    new EmpRefsLinks(self = Link.empRefsLink(apiBaseUrl))
}

case class EmpRefLinks(
  self: Link,
  statements: Link,
  currentStatement: Link
)

object EmpRefLinks {
  def apply(apiBaseUrl: String, empRef: EmpRef): EmpRefLinks =
    EmpRefLinks(
      self = Link.summaryLink(apiBaseUrl, empRef),
      statements = Link.statementsLink(apiBaseUrl, empRef),
      currentStatement = Link.anualStatementLink(apiBaseUrl, empRef, taxYear = TaxYear(TaxYearResolver.currentTaxYear))
    )
//    EmpRefLinks(summary = Link.summaryLink(apiBaseUrl, empRef))
}

