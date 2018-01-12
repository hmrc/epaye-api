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
import uk.gov.hmrc.epayeapi.models.JsonFixtures.emptyEpayeAnnualStatement
import uk.gov.hmrc.epayeapi.models.in._
import uk.gov.hmrc.epayeapi.models.{TaxMonth, TaxYear}

class AnnualStatementJsonSpec extends WordSpec with Matchers {
  val apiBaseUrl = "[API_BASE_URL]"
  val empRef = EmpRefGenerator.getEmpRef
  val dueDate = new LocalDate(2017, 5, 22)

  val taxYear = TaxYear(2016)

  "AnnualStatementJson.apply._links" should {
    "contain the right links" in {
      AnnualStatementJson(apiBaseUrl, empRef, taxYear, emptyEpayeAnnualStatement)._links shouldBe
        AnnualStatementLinksJson(
          empRefs = Link.empRefsLink(apiBaseUrl),
          statements = Link.statementsLink(apiBaseUrl, empRef),
          self = Link.anualStatementLink(apiBaseUrl, empRef, taxYear),
          next = Link.anualStatementLink(apiBaseUrl, empRef, taxYear.next),
          previous = Link.anualStatementLink(apiBaseUrl, empRef, taxYear.previous)
        )
    }
  }

  "AnnualStatementJson.apply._embedded.earlierYearUpdate" should {
    "contain the earlier year update if it is present" in {
      val emptyTotals = AnnualTotal(
        charges = 0,
        payments = 0,
        credits = 0,
        writeOffs = 0,
        balance = 0
      )

      val epayeAnnualStatement =
        emptyEpayeAnnualStatement
          .copy(
            rti =
              AnnualStatementTable(
                lineItems = Seq(
                  LineItem(
                    taxYear = taxYear,
                    taxMonth = None,
                    charges = 100,
                    payments = 10,
                    credits = 20,
                    writeOffs = 0,
                    balance = 100 - 20 - 10,
                    dueDate = dueDate,
                    isSpecified = false,
                    codeText = None,
                    itemType = Some("eyu")
                  )
                ),
                totals = emptyTotals
              )
          )

      AnnualStatementJson(apiBaseUrl, empRef, taxYear, epayeAnnualStatement)._embedded.earlierYearUpdate shouldBe
        Some(EarlierYearUpdateJson(
          amount = 100,
          clearedByCredits = 20,
          clearedByPayments = 10,
          clearedByWriteOffs = 0,
          balance = 100 - 10 - 20,
          dueDate = dueDate
        ))
    }
    "return a None if it is not present" in {
      AnnualStatementJson(apiBaseUrl, empRef, taxYear, emptyEpayeAnnualStatement)._embedded.earlierYearUpdate shouldBe None
    }
  }

  "MonthlyChargesJson.from(lineItem)" should {
    "convert an rti charge from the epaye annual statement" in {
      val taxMonth = TaxMonth(taxYear, 2)

      val lineItem =
        LineItem(
          taxYear = taxYear,
          taxMonth = Some(EpayeTaxMonth(taxMonth.month)),
          charges = 100,
          payments = 10,
          credits = 20,
          writeOffs = 0,
          balance = 100 - 30,
          dueDate = dueDate,
          isSpecified = true,
          codeText = None,
          itemType = None
        )

      MonthlyChargesJson.from(apiBaseUrl, lineItem, empRef, taxYear) shouldBe
        Some(MonthlyChargesJson(
          taxMonth = TaxMonth(taxYear, taxMonth.month),
          amount = 100,
          clearedByCredits = 20,
          clearedByPayments = 10,
          clearedByWriteOffs = 0,
          balance = 100 - 10 - 20,
          dueDate = dueDate,
          isSpecified = true,
          _links = SelfLink(Link.monthlyStatementLink(apiBaseUrl, empRef, taxYear, taxMonth))
        ))
    }
    "return a None if the taxMonth field is None" in {

      val lineItem =
        LineItem(
          taxYear = taxYear,
          taxMonth = None,
          charges = 100,
          payments = 10,
          credits = 20,
          writeOffs = 0,
          balance = 100 - 30,
          dueDate = dueDate,
          isSpecified = false,
          codeText = None,
          itemType = None
        )

      MonthlyChargesJson.from(apiBaseUrl, lineItem, empRef, taxYear) shouldBe None
    }
  }

  "NonRtiChargesJson.from(lineItem)" should {
    "convert an non rti charge from the epaye annual statement" in {
      val code = "SOME_TEXT"

      val lineItem =
        LineItem(
          taxYear = taxYear,
          taxMonth = None,
          charges = 100,
          payments = 10,
          credits = 20,
          writeOffs = 0,
          balance = 100 - 30,
          dueDate = dueDate,
          isSpecified = false,
          codeText = Some(code),
          itemType = None
        )

      NonRtiChargesJson.from(lineItem, taxYear) shouldBe
        Some(NonRtiChargesJson(
          code = code,
          amount = 100,
          clearedByCredits = 20,
          clearedByPayments = 10,
          clearedByWriteOffs = 0,
          balance = 100 - 10 - 20,
          dueDate = dueDate
        ))
    }
  }
}

