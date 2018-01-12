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
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}
import uk.gov.hmrc.play.test.UnitSpec

class LinkSpecs extends UnitSpec {
  val empRef = EmpRefGenerator.getEmpRef
  val apiBaseUrl = "[API_BASE_URL]"
  "Links" should {
    "generate the right root link" in {
      Link.empRefsLink(apiBaseUrl) shouldEqual Link(s"$apiBaseUrl/organisations/paye/")
    }

    "generate the summary link" in {
      Link.summaryLink(apiBaseUrl, empRef) shouldEqual Link(s"$apiBaseUrl/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}")
    }

    "generate the statements link" in {
      Link.statementsLink(apiBaseUrl, empRef) shouldEqual Link(s"$apiBaseUrl/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements")
    }

    "generate the annual statement link" in {
      Link.anualStatementLink(apiBaseUrl, empRef, TaxYear(2017)) shouldEqual Link(s"$apiBaseUrl/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2017-18")
    }

    "generate the monthly statement link" in {
      Link.monthlyStatementLink(apiBaseUrl, empRef, TaxYear(2017), TaxMonth(TaxYear(2017), 1)) shouldEqual Link(s"$apiBaseUrl/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2017-18/1")
    }
  }
}
