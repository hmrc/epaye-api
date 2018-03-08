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
import org.scalatest.{Matchers, WordSpec}

class EpayeTaxMonthSpec extends WordSpec with Matchers {

  "TaxMonth.fromLocalDate" should {
    "use the previous month if the date is on or before the 5th of the month" in {
      val taxMonths = for (month <- 1 to 12) yield {
        EpayeTaxMonth.fromLocalDate(new LocalDate(2017, month, 5))
      }

      taxMonths shouldEqual Seq(
        EpayeTaxMonth(9),
        EpayeTaxMonth(10),
        EpayeTaxMonth(11),
        EpayeTaxMonth(12),
        EpayeTaxMonth(1),
        EpayeTaxMonth(2),
        EpayeTaxMonth(3),
        EpayeTaxMonth(4),
        EpayeTaxMonth(5),
        EpayeTaxMonth(6),
        EpayeTaxMonth(7),
        EpayeTaxMonth(8)
      )
    }
    "use the current month if the date is on or after the 6th of the month" in {
      val taxMonths = for (month <- 1 to 12) yield {
        EpayeTaxMonth.fromLocalDate(new LocalDate(2017, month, 6))
      }

      taxMonths shouldEqual Seq(
        EpayeTaxMonth(10),
        EpayeTaxMonth(11),
        EpayeTaxMonth(12),
        EpayeTaxMonth(1),
        EpayeTaxMonth(2),
        EpayeTaxMonth(3),
        EpayeTaxMonth(4),
        EpayeTaxMonth(5),
        EpayeTaxMonth(6),
        EpayeTaxMonth(7),
        EpayeTaxMonth(8),
        EpayeTaxMonth(9)
      )
    }
  }
}
