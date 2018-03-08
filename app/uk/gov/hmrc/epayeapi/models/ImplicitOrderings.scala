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

object ImplicitOrderings {
  implicit val localDateDescendingOrdering: Ordering[LocalDate] = Ordering.by[LocalDate, Long](_.toDate.getTime).reverse
  implicit val localDateOptionDescendingOrdering: Ordering[Option[LocalDate]] = new Ordering[Option[LocalDate]] {
    def compare(x: Option[LocalDate], y: Option[LocalDate]): Int =
      (x, y) match {
        case (None, None) => 0
        case (_, None) => -1
        case (None, _) => 1
        case (Some(d1), Some(d2)) => localDateDescendingOrdering.compare(d1, d2)
      }
  }
}
