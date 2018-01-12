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

case class TaxMonth(
  taxYear: TaxYear,
  month: Int
) {
  def asString: String =
    month.toString

  def isFirst: Boolean =
    month == 1

  def isLast: Boolean =
    month == 12

  def firstDay: LocalDate =
    taxYear.firstDay.plusMonths(month - 1)

  def lastDay: LocalDate =
    taxYear.firstDay.plusMonths(month).minusDays(1)

  def next: TaxMonth =
    TaxMonth(
      if (isLast) taxYear.next else taxYear,
      if (isLast) 1 else month + 1
    )

  def previous: TaxMonth =
    TaxMonth(
      if (isFirst) taxYear.previous else taxYear,
      if (isFirst) 12 else month - 1
    )
}
