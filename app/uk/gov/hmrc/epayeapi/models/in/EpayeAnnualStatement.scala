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

case class LineItem(
  taxYear: TaxYear,
  taxMonth: Option[EpayeTaxMonth],
  charges: BigDecimal,
  payments: BigDecimal,
  credits: BigDecimal,
  writeOffs: BigDecimal,
  balance: BigDecimal,
  dueDate: LocalDate,
  isSpecified: Boolean = false,
  codeText: Option[String] = None,
  itemType: Option[String] = None
)

case class AnnualTotal(
  charges: BigDecimal,
  payments: BigDecimal,
  credits: BigDecimal,
  writeOffs: BigDecimal,
  balance: BigDecimal
)

case class AnnualStatementTable(lineItems: Seq[LineItem], totals: AnnualTotal)

case class EpayeAnnualStatement(rti: AnnualStatementTable, nonRti: AnnualStatementTable, unallocated: Option[BigDecimal])