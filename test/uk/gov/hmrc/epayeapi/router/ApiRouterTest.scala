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


package uk.gov.hmrc.epayeapi.router


import uk.gov.hmrc.epayeapi.router.ApiRouter.{TaxOfficeNumber, TaxOfficeReference}
import uk.gov.hmrc.play.test.UnitSpec

import scala.util.Random

class ApiRouterSpec extends UnitSpec {
  "ApiRouter.TaxOfficeNumber" should {
    "extract any 3 digit value" in {
      for(i <- 100 to 999)
        TaxOfficeNumber.unapply(i.toString) shouldBe Some(i.toString)
    }

    "not extract any 2 digit value" in {
      for(i <- 10 to 99)
        TaxOfficeNumber.unapply(i.toString) shouldBe None
    }

    "not extract any 1 digit value" in {
      for(i <- 1 to 9)
        TaxOfficeNumber.unapply(i.toString) shouldBe None
    }

    "not extract any 4 digit or more value" in {
      for(_ <- 1 to 100) {
        val i = Random.nextInt(10)

        TaxOfficeNumber.unapply((i * 1000).toString) shouldBe None
      }
    }

    "not extract any character values" in {
      TaxOfficeNumber.unapply("AAA") shouldBe None
    }

    "not extract any mixed values" in {
      TaxOfficeNumber.unapply("AA1") shouldBe None
    }
  }

  "ApiRouter.TaxOfficeReference" should {
    "extract any digit value shorter than 10 characters" in {
      for (length <- 1 to 10)
        TaxOfficeReference.unapply("1" * length) shouldBe Some("1" * length)
    }

    "not extract any digit value longer than 10 characters" in {
      for (length <- 11 to 30)
        TaxOfficeReference.unapply("1" * length) shouldBe None
    }

    "extract any uppercase character value shorter than 10 characters" in {
      for (length <- 1 to 10)
        TaxOfficeReference.unapply("A" * length) shouldBe Some("A" * length)
    }

    "not extract any uppercase character value longer than 10 characters" in {
      for (length <- 11 to 30)
        TaxOfficeReference.unapply("A" * length) shouldBe None
    }

    "not extract any lowercase characters" in {
      for (length <- 1 to 30)
        TaxOfficeReference.unapply("a" * length) shouldBe None
    }
  }


}
