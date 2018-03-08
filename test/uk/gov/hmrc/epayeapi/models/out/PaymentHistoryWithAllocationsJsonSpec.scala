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

import common.EmpRefGenerator
import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}
import uk.gov.hmrc.epayeapi.models.in._

class PaymentHistoryWithAllocationsJsonSpec extends WordSpec with Matchers {
  val apiBaseUrl = "[API_BASE_URL]"
  val empRef = EmpRefGenerator.getEmpRef

  "PaymentHistoryWithAllocationsJson.transform" should {
    val taxYear = TaxYear(2016)

    val _links = PaymentHistoryWithAllocationsLinks(
      Link.empRefsLink,
      Link.summaryLink(empRef),
      Link.statementsLink(empRef),
      Link.paymentHistoryLink(empRef, taxYear),
      Link.paymentHistoryWithAllocationsLink(empRef, taxYear),
      Link.paymentHistoryWithAllocationsLink(empRef, taxYear.next),
      Link.paymentHistoryWithAllocationsLink(empRef, taxYear.previous)
    )

    "return empty payment history for tax year 2016-17" in {
      val epayePaymentHistoryWithAllocations = EpayePaymentHistoryWithAllocations(taxYear, Seq.empty)

      val expectedPaymentHistoryWithAllocations = PaymentHistoryWithAllocationsJson(
        empRef.taxOfficeNumber,
        empRef.taxOfficeReference,
        taxYear,
        Seq.empty,
        _links
      )

      PaymentHistoryWithAllocationsJson.transform(apiBaseUrl, empRef, taxYear, epayePaymentHistoryWithAllocations) shouldBe
        expectedPaymentHistoryWithAllocations
    }

    "return payments, sorted by date & amount, for tax year 2016-17" in {
      val taxMonth1  = EpayeTaxPeriod(taxYear.firstDay, taxYear.firstDay.plusMonths(1).minusDays(1))
      val taxMonth2  = taxMonth1.plusTaxMonths(1)
      val taxMonth3  = taxMonth2.plusTaxMonths(1)
      val taxMonth4  = taxMonth3.plusTaxMonths(1)
      val taxMonth5  = taxMonth4.plusTaxMonths(1)
      val taxMonth10 = taxMonth5.plusTaxMonths(5)
      val taxMonth11 = taxMonth10.plusTaxMonths(1)

      val epayePaymentHistory = EpayePaymentHistoryWithAllocations(
        taxYear,
        Seq(
          EpayePaymentHistoryWithAllocationsPayment(
            paymentDate = Some(new LocalDate(2016,6,17)),
            method = Some("TPS RECEIPTS BY DEBIT CARD"),
            amount = 123.45,
            allocatedAmount = 123.45,
            allocations = Seq(
              EpayeRtiPaymentAllocation(taxMonth2, 100.00),
              EpayeRtiPaymentAllocation(taxMonth1,  23.45)
            )
          ),
          EpayePaymentHistoryWithAllocationsPayment(
            paymentDate = Some(new LocalDate(2016,10,7)),
            method = Some("PAYMENTS MADE BY CHEQUE"),
            amount = 456.78,
            allocatedAmount = 456.78,
            allocations = Seq(
              EpayeNonRtiPaymentAllocation(taxMonth4, 400.00, Some(EpayeCode("NON_RTI_CIS_FIXED_PENALTY"))),
              EpayeRtiPaymentAllocation(taxMonth5,  56.78)
            )
          ),
          EpayePaymentHistoryWithAllocationsPayment(
            paymentDate = Some(new LocalDate(2016,12,8)),
            method = Some("TPS RECEIPTS BY CREDIT CARD"),
            amount = 999.00,
            allocatedAmount = 999.00,
            allocations = Seq(
              EpayeRtiPaymentAllocation(taxMonth10, 900.00),
              EpayeNonRtiPaymentAllocation(taxMonth11,  99.00, Some(EpayeCode("NON_RTI_EI_LATE_REPORT_DAILY_PENALTY")))
            )
          ),
          EpayePaymentHistoryWithAllocationsPayment(
            paymentDate = Some(new LocalDate(2016,10,7)),
            method = Some("BACS RECEIPTS"),
            amount = 111.11,
            allocatedAmount = 111.11,
            allocations = Seq()
          )
        )
      )

      val expectedPaymentHistoryWithAllocations = PaymentHistoryWithAllocationsJson(
        empRef.taxOfficeNumber,
        empRef.taxOfficeReference,
        taxYear,
        Seq(
          PaymentWithAllocations(new LocalDate(2016,12,8), Some("Credit Card"), 999.00, 999.00, Seq(
            PaymentAllocation(taxYear, TaxMonth(taxYear, 10), 900.00, None),
            PaymentAllocation(taxYear, TaxMonth(taxYear, 11),  99.00, Some("NON_RTI_EI_LATE_REPORT_DAILY_PENALTY"))
          )),
          PaymentWithAllocations(new LocalDate(2016,10,7), Some("Cheque"), 456.78, 456.78, Seq(
            PaymentAllocation(taxYear, TaxMonth(taxYear, 4), 400.00, Some("NON_RTI_CIS_FIXED_PENALTY")),
            PaymentAllocation(taxYear, TaxMonth(taxYear, 5),  56.78, None)
          )),
          PaymentWithAllocations(new LocalDate(2016,10,7), Some("BACS"), 111.11, 111.11, Seq()),
          PaymentWithAllocations(new LocalDate(2016,6,17), Some("Debit Card"), 123.45, 123.45, Seq(
            PaymentAllocation(taxYear, TaxMonth(taxYear, 2), 100.00, None),
            PaymentAllocation(taxYear, TaxMonth(taxYear, 1),  23.45, None)
          ))
        ),
        _links
      )

      PaymentHistoryWithAllocationsJson.transform(apiBaseUrl, empRef, taxYear, epayePaymentHistory) shouldBe
        expectedPaymentHistoryWithAllocations
    }
  }

  implicit class EpayeTaxPeriodOps(period: EpayeTaxPeriod) {
    def plusTaxMonths(months: Int): EpayeTaxPeriod = {
      period.copy(taxFrom = period.taxFrom.plusMonths(months), taxTo = period.taxTo.plusMonths(months))
    }
  }
}
