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

case class EpayeTaxPeriod(taxFrom: LocalDate, taxTo: LocalDate) {
  /**
    * Gets the TaxMonth from a tax period. This requires the period's start date to be on the 6th of the given month
    * and the end date to be on the 5th of the next month otherwise it is considered invalid
    *
    * Note: For quirky reasons which will go away soon, the start date is also valid if it is on the 4th...
    *
    */
  def taxMonth: Option[Int] = {
    implicit val dateAscendingOrdering = uk.gov.hmrc.epayeapi.models.ImplicitOrderings.localDateDescendingOrdering.reverse

    val Seq(lowerDate, upperDate) = Seq(taxFrom, taxTo).sorted
    val expectedTo1 = lowerDate.plusMonths(1).minusDays(1)
    val isTaxToCorrect1 = expectedTo1 == upperDate
    (lowerDate.getMonthOfYear, lowerDate.getDayOfMonth) match {
      case (m, 6) if isTaxToCorrect1 => Some(((m + 8) % 12) + 1)
      case _ => None
    }
  }

  def taxYear: Option[Int] =
    if (taxFrom == startOfTaxYear && taxTo == endOfTaxYear) {
      Some(taxFrom.getYear)
    } else {
      None
    }

  def startingTaxYear: Int =
    if (taxFrom.isBefore(startOfTaxYear)) {
      taxFrom.getYear - 1
    } else {
      taxFrom.getYear
    }

  private def startOfTaxYear: LocalDate = new LocalDate(taxFrom.getYear, 4, 6)

  private def endOfTaxYear: LocalDate = startOfTaxYear.plusYears(1).minusDays(1)
}
