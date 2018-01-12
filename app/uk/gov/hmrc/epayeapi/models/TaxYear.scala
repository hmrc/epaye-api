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

package uk.gov.hmrc.epayeapi.models

import org.joda.time.LocalDate
import uk.gov.hmrc.time.TaxYearResolver

import scala.util.{Success, Try}

case class TaxYear(
  yearFrom: Int
) {
  def asString: String =
    s"$yearFrom-${(yearFrom + 1) % 100}"

  def next: TaxYear =
    TaxYear(yearFrom + 1)

  def previous: TaxYear =
    TaxYear(yearFrom - 1)

  def firstDay: LocalDate =
    TaxYearResolver.startOfTaxYear(yearFrom)

  def lastDay: LocalDate =
    firstDay.plusYears(1).minusDays(1)
}

object ExtractTaxYear {
  private lazy val pattern = """20(\d\d)-(\d\d)""".r

  def unapply(taxYear: String): Option[TaxYear] = {
    taxYear match {
      case pattern(fromYear, toYear) =>
        Try(toYear.toInt - fromYear.toInt) match {
          case Success(1) => Some(TaxYear(2000 + fromYear.toInt))
          case _ => None
        }
      case _ => None
    }
  }
}