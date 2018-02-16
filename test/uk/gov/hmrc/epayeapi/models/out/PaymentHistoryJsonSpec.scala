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
import uk.gov.hmrc.epayeapi.models.TaxYear
import uk.gov.hmrc.epayeapi.models.in.{EpayePaymentHistory, EpayePaymentHistoryPayment}

class PaymentHistoryJsonSpec extends WordSpec with Matchers {
  val apiBaseUrl = "[API_BASE_URL]"
  val empRef = EmpRefGenerator.getEmpRef

  "PaymentHistoryJson.transform" should {
    val taxYear = TaxYear(2016)

    val _links = PaymentHistoryLinks(
      Link.empRefsLink,
      Link.summaryLink(empRef),
      Link.statementsLink(empRef),
      Link.paymentHistoryLink(empRef, taxYear),
      Link.paymentHistoryLink(empRef, taxYear.next),
      Link.paymentHistoryLink(empRef, taxYear.previous)
    )

    "return empty payment history for tax year 2016-17" in {
      val epayePaymentHistory = EpayePaymentHistory(Seq.empty)

      val expectedPaymentHistory = PaymentHistoryJson(
        empRef.taxOfficeNumber,
        empRef.taxOfficeReference,
        taxYear,
        Seq.empty,
        _links
      )

      PaymentHistoryJson.transform(apiBaseUrl, empRef, taxYear, epayePaymentHistory) shouldBe expectedPaymentHistory
    }

    "return payments, sorted by date & amount, for tax year 2016-17" in {

      val epayePaymentHistory = EpayePaymentHistory(
        Seq(
          EpayePaymentHistoryPayment(
            dateOfPayment = Some(new LocalDate(2016,6,17)),
            amount = 123.45
          ),
          EpayePaymentHistoryPayment(
            dateOfPayment = Some(new LocalDate(2016,10,7)),
            amount = 456.78
          ),
          EpayePaymentHistoryPayment(
            dateOfPayment = Some(new LocalDate(2016,12,8)),
            amount = 999.00
          ),
          EpayePaymentHistoryPayment(
            dateOfPayment = Some(new LocalDate(2016,10,7)),
            amount = 111.11
          )
        )
      )

      val expectedPaymentHistory = PaymentHistoryJson(
        empRef.taxOfficeNumber,
        empRef.taxOfficeReference,
        taxYear,
        Seq(
          Payment(new LocalDate(2016,12,8), 999.00),
          Payment(new LocalDate(2016,10,7), 456.78),
          Payment(new LocalDate(2016,10,7), 111.11),
          Payment(new LocalDate(2016,6,17), 123.45)
        ),
        _links
      )

      PaymentHistoryJson.transform(apiBaseUrl, empRef, taxYear, epayePaymentHistory) shouldBe expectedPaymentHistory
    }
  }
}
