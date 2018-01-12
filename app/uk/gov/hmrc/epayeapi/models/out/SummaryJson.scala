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
import uk.gov.hmrc.epayeapi.models.in.EpayeTotalsResponse

case class Breakdown(
  rti: BigDecimal,
  nonRti: BigDecimal
)

case class OutstandingCharges(
  amount: BigDecimal,
  breakdown: Breakdown
)

case class SummaryJson(
  outstandingCharges: OutstandingCharges,
  _links: SummaryLinks
)

object SummaryJson {
  def apply(apiBaseUrl: String, empRef: EmpRef, total: EpayeTotalsResponse): SummaryJson =
    SummaryJson(
      OutstandingCharges(
        total.overall,
        Breakdown(
          rti = total.rti.totals.balance,
          nonRti = total.nonRti.totals.balance
        )
      ),
      SummaryLinks(apiBaseUrl, empRef)
    )
}

case class SummaryLinks(
  empRefs: Link,
  self: Link
)

object SummaryLinks {
  def apply(apiBaseUrl: String, empRef: EmpRef): SummaryLinks = SummaryLinks(
    Link.empRefsLink(apiBaseUrl),
    Link.summaryLink(apiBaseUrl, empRef)
  )
}

