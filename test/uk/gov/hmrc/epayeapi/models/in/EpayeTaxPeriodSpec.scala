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
import uk.gov.hmrc.play.test.UnitSpec

class EpayeTaxPeriodSpec extends UnitSpec {
  implicit def tupleThree2LocalDate(tuple: (Int, Int, Int)): LocalDate =
    tuple match {
      case (year, month, day) => new LocalDate(year, month, day)
    }

  implicit def tupleTwo2LocalDate(tuple: (Int, Int)): LocalDate =
    tuple match {
      case (month, day) => new LocalDate(2000, month, day)
    }

  "A tax period" should {
    "return the correct tax month" in {
      EpayeTaxPeriod(taxFrom = (4, 6), taxTo = (5, 5)).taxMonth should contain(1)
      EpayeTaxPeriod(taxFrom = (5, 6), taxTo = (6, 5)).taxMonth should contain(2)
      EpayeTaxPeriod(taxFrom = (6, 6), taxTo = (7, 5)).taxMonth should contain(3)
      EpayeTaxPeriod(taxFrom = (7, 6), taxTo = (8, 5)).taxMonth should contain(4)
      EpayeTaxPeriod(taxFrom = (8, 6), taxTo = (9, 5)).taxMonth should contain(5)
      EpayeTaxPeriod(taxFrom = (9, 6), taxTo = (10, 5)).taxMonth should contain(6)
      EpayeTaxPeriod(taxFrom = (10, 6), taxTo = (11, 5)).taxMonth should contain(7)
      EpayeTaxPeriod(taxFrom = (11, 6), taxTo = (12, 5)).taxMonth should contain(8)
      EpayeTaxPeriod(taxFrom = (2000, 12, 6), taxTo = (2001, 1, 5)).taxMonth should contain(9)
      EpayeTaxPeriod(taxFrom = (1, 6), taxTo = (2, 5)).taxMonth should contain(10)
      EpayeTaxPeriod(taxFrom = (2, 6), taxTo = (3, 5)).taxMonth should contain(11)
      EpayeTaxPeriod(taxFrom = (3, 6), taxTo = (4, 5)).taxMonth should contain(12)
    }
    "return no tax month if the dates don't match" in {
      EpayeTaxPeriod(taxFrom = (5, 12), taxTo = (6, 13)).taxMonth shouldBe empty
      EpayeTaxPeriod(taxFrom = (6, 26), taxTo = (7, 27)).taxMonth shouldBe empty
      // Skip a month
      EpayeTaxPeriod(taxFrom = (4, 6), taxTo = (6, 5)).taxMonth shouldBe empty
      // Skip a year
      EpayeTaxPeriod(taxFrom = (2000, 4, 6), taxTo = (2001, 5, 5)).taxMonth shouldBe empty
      EpayeTaxPeriod(taxFrom = (5, 4), taxTo = (6, 5)).taxMonth shouldBe empty
    }
    "return the correct beginning tax year" in {
      EpayeTaxPeriod(taxFrom = (2000, 1, 1), taxTo = (2000, 2, 1)).startingTaxYear shouldBe 1999
      EpayeTaxPeriod(taxFrom = (2000, 4, 5), taxTo = (2000, 5, 5)).startingTaxYear shouldBe 1999
      EpayeTaxPeriod(taxFrom = (2000, 4, 6), taxTo = (2000, 5, 6)).startingTaxYear shouldBe 2000
      EpayeTaxPeriod(taxFrom = (2000, 12, 31), taxTo = (2001, 1, 31)).startingTaxYear shouldBe 2000
    }
  }
}