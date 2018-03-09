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

import org.scalatest.{Matchers, WordSpec}

class PaymentJsonSpec extends WordSpec with Matchers {
  "PaymentJson.transform" should {

    "convert each payment method code to a text string" in {

      val expectedMappings: Map[String, String] = Map(
        "TPS RECEIPTS BY DEBIT CARD" -> "Debit Card",
        "PAYMENTS MADE BY CHEQUE" -> "Cheque",
        "CHEQUE RECEIPTS" -> "Cheque",
        "BACS RECEIPTS" -> "BACS",
        "CHAPS" -> "CHAPS",
        "TPS RECEIPTS BY CREDIT CARD" -> "Credit Card",
        "NATIONAL DIRECT DEBIT RECEIPTS" -> "Direct Debit",
        "BILLPAY/OLPG/GIROBANK" -> "Online Payment",
        "BANK LODGEMENT PAYMENT" -> "Bank Lodgement",
        "BANK GIRO RECEIPTS" -> "Giro Receipts",
        "BANK GIRO IN CREDITS" -> "Giro Credits",
        "FPS RECEIPTS" -> "FPS Receipts",
        "CREDIT FOR INTERNET RECEIPTS" -> "Internet Receipts",
        "GIROBANK RECEIPTS" -> "Girobank",
        "GIROBANK/ POST OFFICE" -> "Post Office",
        "NIL DECLARATIONS" -> "Nil Declarations",
        "PAYMASTER" -> "Paymaster",
        "VOLUNTARY DIRECT PAYMENTS" -> "Voluntary Payments"
      )

      expectedMappings foreach {
        case (code, text) => PaymentJson.transform(Some(code)) shouldBe Some(text)
      }
    }

    "convert unknown payment code to UNKNOWN" in {
      PaymentJson.transform(Some("Blah")) shouldBe Some("UNKNOWN")
    }

    "convert missing payment code to None" in {
      PaymentJson.transform(None) shouldBe None
    }
  }
}
