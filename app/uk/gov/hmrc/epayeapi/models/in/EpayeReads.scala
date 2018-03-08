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

import play.api.libs.json.Json.reads
import play.api.libs.json._
import uk.gov.hmrc.epayeapi.models.TaxYear

trait EpayeReads {
  implicit lazy val taxYearReads: Reads[TaxYear] = reads[TaxYear]
  implicit lazy val epayeTaxMonthReads: Reads[EpayeTaxMonth] = reads[EpayeTaxMonth]
  implicit lazy val epayeTaxPeriodReads: Reads[EpayeTaxPeriod] = reads[EpayeTaxPeriod]
  implicit lazy val epayeCodeReads: Reads[EpayeCode] = new Reads[EpayeCode]() {
    override def reads(json: JsValue): JsResult[EpayeCode] =
      json match {
        case JsString(name) => JsSuccess(EpayeCode(name))
        case _ => JsError()
      }
  }

  implicit lazy val annualTotalReads: Reads[AnnualTotal] = reads[AnnualTotal]
  implicit lazy val lineItemReads: Reads[LineItem] = reads[LineItem]

  implicit lazy val epayeTotals: Reads[EpayeTotals] = reads[EpayeTotals]
  implicit lazy val epayeTotalsItems: Reads[EpayeTotalsItem] = reads[EpayeTotalsItem]
  implicit lazy val epayeTotalsResponse: Reads[EpayeTotalsResponse] = reads[EpayeTotalsResponse]

  implicit lazy val annualStatementTableReads: Reads[AnnualStatementTable] = reads[AnnualStatementTable]
  implicit lazy val epayeAnnualStatementReads: Reads[EpayeAnnualStatement] = reads[EpayeAnnualStatement]

  implicit lazy val epayeMonthlyStatementReads: Reads[EpayeMonthlyStatement] = reads[EpayeMonthlyStatement]

  implicit lazy val epayeMonthlyChargesReads: Reads[EpayeMonthlyCharges] = reads[EpayeMonthlyCharges]
  implicit lazy val epayeMonthlyCreditsReads: Reads[EpayeMonthlyCredits] = reads[EpayeMonthlyCredits]
  implicit lazy val epayeMonthlyUnknownCreditsDetailsReads: Reads[EpayeMonthlyUnknownCreditsDetails] = reads[EpayeMonthlyUnknownCreditsDetails]
  implicit lazy val epayeMonthlyChargesDetailsReads: Reads[EpayeMonthlyChargesDetails] = reads[EpayeMonthlyChargesDetails]
  implicit lazy val epayeMonthlyStatementItemReads: Reads[EpayeMonthlyStatementItem] = reads[EpayeMonthlyStatementItem]
  implicit lazy val epayeMonthlyPaymentDetailsReads: Reads[EpayeMonthlyPaymentDetails] = reads[EpayeMonthlyPaymentDetails]
  implicit lazy val epayeMonthlyPaymentItemReads: Reads[EpayeMonthlyPaymentItem] = reads[EpayeMonthlyPaymentItem]
  implicit lazy val epayeMonthlyBalanceReads: Reads[EpayeMonthlyBalance] = reads[EpayeMonthlyBalance]

  implicit lazy val epayeMasterDataResponse: Reads[EpayeMasterData] = reads[EpayeMasterData]

  implicit lazy val epayePaymentHistoryReads: Reads[EpayePaymentHistory] = reads[EpayePaymentHistory]
  implicit lazy val epayePaymentHistoryPaymentReads: Reads[EpayePaymentHistoryPayment] = reads[EpayePaymentHistoryPayment]

  implicit lazy val epayePaymentHistoryWithAllocationsReads: Reads[EpayePaymentHistoryWithAllocations] = reads[EpayePaymentHistoryWithAllocations]
  implicit lazy val epayePaymentHistoryWithAllocationsPaymentReads: Reads[EpayePaymentHistoryWithAllocationsPayment] = reads[EpayePaymentHistoryWithAllocationsPayment]
  implicit lazy val epayeRtiPaymentAllocationReads: Reads[EpayeRtiPaymentAllocation] = reads[EpayeRtiPaymentAllocation]
  implicit lazy val epayeNonRtiPaymentAllocationReads: Reads[EpayeNonRtiPaymentAllocation] = reads[EpayeNonRtiPaymentAllocation]
  implicit lazy val epayePaymentAllocationReads: Reads[EpayePaymentAllocation] = new Reads[EpayePaymentAllocation]() {
    override def reads(json: JsValue): JsResult[EpayePaymentAllocation] = {
      json \ "code" match {
        case JsDefined(_) => epayeNonRtiPaymentAllocationReads.reads(json)
        case _ => epayeRtiPaymentAllocationReads.reads(json)
      }
    }
  }

  implicit lazy val epayeEmpRefsResponse: Format[EpayeEmpRefsResponse] = Json.format[EpayeEmpRefsResponse]
}
