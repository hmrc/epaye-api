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

import play.api.libs.json.Json.{obj, writes}
import play.api.libs.json.{JsString, JsValue, Json, Writes}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}

trait JsonWrites {
  implicit lazy val empRefWrites: Writes[EmpRef] = new Writes[EmpRef] {
    override def writes(o: EmpRef): JsValue = JsString(s"${o.taxOfficeNumber}/${o.taxOfficeReference}")
  }
  implicit lazy val linkWrites: Writes[Link] = writes[Link]
  implicit lazy val taxYearWrites: Writes[TaxYear] = new Writes[TaxYear] {
    override def writes(taxYear: TaxYear): JsValue =
      obj(
        "year" -> taxYear.asString,
        "firstDay" -> taxYear.firstDay,
        "lastDay" -> taxYear.lastDay
      )
  }
  implicit lazy val taxMonthWrites: Writes[TaxMonth] = new Writes[TaxMonth] {
    override def writes(taxMonth: TaxMonth): JsValue =
      obj(
        "month" -> taxMonth.month,
        "firstDay" -> taxMonth.firstDay,
        "lastDay" -> taxMonth.lastDay
      )
  }

  implicit lazy val empRefsLinksWrites: Writes[EmpRefsLinks] = writes[EmpRefsLinks]
  implicit lazy val empRefLinksWrites: Writes[EmpRefLinks] = writes[EmpRefLinks]
  implicit lazy val empRefItemWrites: Writes[EmpRefItem] = writes[EmpRefItem]
  implicit lazy val embeddedEmpRefsJsonWrites: Writes[EmbeddedEmpRefs] = writes[EmbeddedEmpRefs]
  implicit lazy val empRefsJsonWrites: Writes[EmpRefsJson] = writes[EmpRefsJson]
  implicit lazy val breakdownWrites: Writes[Breakdown] = writes[Breakdown]
  implicit lazy val outstandingChargesWrites: Writes[OutstandingCharges] = writes[OutstandingCharges]
  implicit lazy val summaryLinksWrites: Writes[SummaryLinks] = writes[SummaryLinks]
  implicit lazy val summaryJsonWrites: Writes[SummaryJson] = writes[SummaryJson]
  implicit lazy val apiErrorWrites: Writes[ApiErrorJson] = writes[ApiErrorJson]

  implicit lazy val annualStatementJsonWrites: Writes[AnnualStatementJson] = writes[AnnualStatementJson]
  implicit lazy val annualStatementLinksJsonWrites: Writes[AnnualStatementLinksJson] = writes[AnnualStatementLinksJson]
  implicit lazy val selfLinkWrites: Writes[SelfLink] = writes[SelfLink]
  implicit lazy val monthlyChargesJsonWrites: Writes[MonthlyChargesJson] = writes[MonthlyChargesJson]
  implicit lazy val embeddedRtiChargesJsonWrites: Writes[EmbeddedRtiChargesJson] = writes[EmbeddedRtiChargesJson]
  implicit lazy val earlierYearUpdateJsonWrites: Writes[EarlierYearUpdateJson] = writes[EarlierYearUpdateJson]
  implicit lazy val periodJsonWrites: Writes[PeriodJson] = writes[PeriodJson]
  implicit lazy val nonRtiChargesJsonWrites: Writes[NonRtiChargesJson] = writes[NonRtiChargesJson]

  implicit lazy val monthlyStatementJsonWrites: Writes[MonthlyStatementJson] = writes[MonthlyStatementJson]
  implicit lazy val chargeJsonWrites: Writes[ChargeJson] = writes[ChargeJson]
  implicit lazy val paymentJsonWrites: Writes[PaymentJson] = writes[PaymentJson]
  implicit lazy val monthlySummaryJsonWrites: Writes[MonthlySummaryJson] = writes[MonthlySummaryJson]
  implicit lazy val monthlyStatementLinksJsonWrites: Writes[MonthlyStatementLinksJson] = writes[MonthlyStatementLinksJson]
}
