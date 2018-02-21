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
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.TaxYear
import uk.gov.hmrc.epayeapi.models.out.Link.prefix
import uk.gov.hmrc.epayeapi.util.TestTimeMachine.withFixedLocalDate

class StatementsJsonSpec extends WordSpec with Matchers {
  val empRef = EmpRefGenerator.getEmpRef

  val _links = Links(
    empRefs = Link.empRefsLink,
    self = Link.statementsLink(empRef)
  )

  "StatementsJson.apply" should {
    "return a result with empty statements when the year of registration is missing" in {
      StatementsJson.apply(empRef, None) shouldBe
        StatementsJson(empRef.taxOfficeNumber, empRef.taxOfficeReference, Embedded(Seq.empty), _links)
    }
    "return a result with a statement link for the current tax year" in {
      val currentTaxYear = TaxYear(LocalDate.now())

      val currentTaxYearStatement = Statement(
        currentTaxYear,
        StatementLinks(annualStatementLink(currentTaxYear))
      )
      val _embedded = Embedded(Seq(currentTaxYearStatement))

      StatementsJson.apply(empRef, Some(currentTaxYear)) shouldBe
        StatementsJson(empRef.taxOfficeNumber, empRef.taxOfficeReference, _embedded, _links)
    }
    "return a result with statement links for 2015, 2016 and 2017" in {
      val yearOfRegistration = 2015
      val currentTaxYearFrom = 2017

      val today = new LocalDate(currentTaxYearFrom, 6, 6)

      withFixedLocalDate(today) {
        val statements = for {
          taxYearFrom <- yearOfRegistration to currentTaxYearFrom
          taxYear = TaxYear(taxYearFrom)
        } yield Statement(taxYear, StatementLinks(annualStatementLink(taxYear)))

        val _embedded = Embedded(statements)

        StatementsJson.apply(empRef, Some(TaxYear(yearOfRegistration))) shouldBe
          StatementsJson(empRef.taxOfficeNumber, empRef.taxOfficeReference, _embedded, _links)
      }
    }
  }

  private def annualStatementLink(taxYear: TaxYear, eRef: EmpRef = empRef): Link =
    Link(s"$prefix/${eRef.taxOfficeNumber}/${eRef.taxOfficeReference}/statements/${taxYear.asString}")
}
