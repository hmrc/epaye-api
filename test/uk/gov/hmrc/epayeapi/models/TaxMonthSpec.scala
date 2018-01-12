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
import org.scalatest.{Matchers, WordSpec}

class TaxMonthSpec extends WordSpec with Matchers {

  "TaxMonth" should {
    "calculate start and end dates correctly" in {
      TaxMonth(TaxYear(2015), 1).firstDay shouldBe new LocalDate(2015, 4, 6)
      TaxMonth(TaxYear(2015), 1).lastDay shouldBe new LocalDate(2015, 5, 5)

      TaxMonth(TaxYear(2015), 2).firstDay shouldBe new LocalDate(2015, 5, 6)
      TaxMonth(TaxYear(2015), 2).lastDay shouldBe new LocalDate(2015, 6, 5)

      TaxMonth(TaxYear(2015), 9).firstDay shouldBe new LocalDate(2015, 12, 6)
      TaxMonth(TaxYear(2015), 9).lastDay shouldBe new LocalDate(2016, 1, 5)

      TaxMonth(TaxYear(2015), 11).firstDay shouldBe new LocalDate(2016, 2, 6)
      TaxMonth(TaxYear(2015), 11).lastDay shouldBe new LocalDate(2016, 3, 5)

      TaxMonth(TaxYear(2015), 12).firstDay shouldBe new LocalDate(2016, 3, 6)
      TaxMonth(TaxYear(2015), 12).lastDay shouldBe new LocalDate(2016, 4, 5)
    }
  }
}
