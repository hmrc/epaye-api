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
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}

case class Link(href: String)

object Link {
  val prefix = "/organisations/paye"

  def empRefsLink(apiBaseUrl: String): Link =
    Link(s"$apiBaseUrl$prefix/")

  def summaryLink(apiBaseUrl: String, empRef: EmpRef): Link =
    Link(s"$apiBaseUrl$prefix/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}")

  def statementsLink(apiBaseUrl: String, empRef: EmpRef): Link =
    Link(s"$apiBaseUrl$prefix/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements")

  def anualStatementLink(apiBaseUrl: String, empRef: EmpRef, taxYear: TaxYear): Link =
    Link(s"$apiBaseUrl$prefix/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/${taxYear.asString}")

  def monthlyStatementLink(apiBaseUrl: String, empRef: EmpRef, taxYear: TaxYear, taxMonth: TaxMonth): Link =
    Link(s"$apiBaseUrl$prefix/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/${taxYear.asString}/${taxMonth.asString}")
}
